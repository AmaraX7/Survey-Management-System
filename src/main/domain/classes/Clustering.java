package main.domain.classes;

import java.util.*;

/**
 * Gestor de clustering encargado de preparar los datos, configurar la estrategia
 * de clustering y ejecutar el algoritmo seleccionado.
 * <p>
 * Esta clase coordina:
 * <ul>
 *   <li>La extracción de respuestas de {@link UsuarioRespondedor} para una {@link Encuesta}.</li>
 *   <li>La construcción del dataset en forma de matriz {@code Object[][]}.</li>
 *   <li>La imputación de valores {@code null} mediante un enfoque KNN.</li>
 *   <li>La configuración de la estrategia ({@link AlgoritmoClustering}).</li>
 *   <li>La ejecución del algoritmo y el montaje del {@link ResultadoClustering}.</li>
 * </ul>
 */
public class Clustering {

    /**
     * Número de vecinos utilizados para la imputación KNN.
     */
    private static final int K_NEIGHBORS = 5;

    /**
     * Crea una instancia del gestor de clustering.
     */
    public Clustering() {
    }

    /**
     * Ejecuta una iteración de clustering:
     * prepara el dataset, configura la estrategia y ejecuta el algoritmo.
     *
     * @param estrategia     implementación concreta de {@link AlgoritmoClustering} a ejecutar
     * @param usuarios       lista de usuarios respondedores a incluir en el clustering
     * @param encuesta       encuesta sobre la que se ejecuta el clustering
     * @param indicePregunta servicio para resolver el índice de una pregunta dentro de la encuesta
     * @return resultado del clustering, enriquecido con los IDs de usuarios y el ID de la encuesta
     */
    public ResultadoClustering ejecutarClustering(
            AlgoritmoClustering estrategia,
            List<UsuarioRespondedor> usuarios,
            Encuesta encuesta,
            IndicePregunta indicePregunta
    ) {

        List<String> idsUsuarios = new ArrayList<>();
        Object[][] datos = prepararDatos(usuarios, encuesta, indicePregunta, idsUsuarios);

        // 2. Configurar estrategia
        TipoPregunta[] tipos = extraerTiposPreguntas(encuesta);
        estrategia.setTipoPreguntas(tipos);
        configurarEstrategia(estrategia, encuesta);

        // 3. Ejecutar estrategia directamente
        ResultadoClustering r = estrategia.execute(datos);
        r.setIdsUsuarios(idsUsuarios);
        r.setIdEncuesta(encuesta.getId());

        return r;
    }

    /**
     * Configura una semilla en la estrategia de clustering, si la implementación lo soporta.
     * <p>
     * Este método realiza comprobaciones por tipo para aquellas estrategias que expongan
     * el método {@code setSeed(long)}.
     *
     * @param estrategia estrategia de clustering a configurar
     * @param semilla    semilla a establecer
     */
    public void configurarSemilla(AlgoritmoClustering estrategia, long semilla) {
        if (estrategia instanceof KMeans) {
            ((KMeans) estrategia).setSeed(semilla);
        } else if (estrategia instanceof KMeansPlusPlus) {
            ((KMeansPlusPlus) estrategia).setSeed(semilla);
        } else if (estrategia instanceof KMedoids) {
            ((KMedoids) estrategia).setSeed(semilla);
        }
    }

    private Object[][] prepararDatos(List<UsuarioRespondedor> usuarios, Encuesta encuesta,
                                     IndicePregunta indicePregunta, List<String> outIds) {
        List<Object[]> todasLasRespuestas = new ArrayList<>();

        for (UsuarioRespondedor usuario : usuarios) {
            Object[] respuestasUsuario = convertirRespuestasAArray(usuario, encuesta, indicePregunta);
            todasLasRespuestas.add(respuestasUsuario);
            outIds.add(usuario.getId());
        }

        imputarValoresNull(todasLasRespuestas, encuesta);

        return todasLasRespuestas.toArray(new Object[0][]);
    }

    private void imputarValoresNull(List<Object[]> datos, Encuesta encuesta) {
        int numUsuarios = datos.size();
        int numPreguntas = datos.isEmpty() ? 0 : datos.get(0).length;

        for (int i = 0; i < numUsuarios; i++) {
            Object[] respuestasUsuario = datos.get(i);
            for (int p = 0; p < numPreguntas; p++) {
                if (respuestasUsuario[p] == null) {
                    respuestasUsuario[p] = imputarConKNN(i, p, datos, encuesta);
                }
            }
        }
    }

    private Object imputarConKNN(int usuarioIdx, int preguntaIdx,
                                 List<Object[]> datos, Encuesta encuesta) {
        List<VecinoDistancia> vecinos = new ArrayList<>();
        Object[] usuarioActual = datos.get(usuarioIdx);

        for (int i = 0; i < datos.size(); i++) {
            if (i == usuarioIdx) continue;

            Object[] otroUsuario = datos.get(i);

            if (otroUsuario[preguntaIdx] != null) {
                double distancia = calcularDistanciaParcial(usuarioActual, otroUsuario, preguntaIdx, encuesta);
                vecinos.add(new VecinoDistancia(i, distancia, otroUsuario[preguntaIdx]));
            }
        }

        if (vecinos.isEmpty()) {
            return obtenerValorPorDefecto(preguntaIdx, encuesta);
        }

        Collections.sort(vecinos, Comparator.comparingDouble(v -> v.distancia));

        int k = Math.min(K_NEIGHBORS, vecinos.size());
        List<VecinoDistancia> kVecinos = vecinos.subList(0, k);

        return calcularValorImputado(kVecinos, preguntaIdx, encuesta);
    }

    private double calcularDistanciaParcial(Object[] usuarioA, Object[] usuarioB,
                                            int preguntaExcluida, Encuesta encuesta) {
        double sumaDistancias = 0.0;
        int preguntasValidas = 0;

        List<Pregunta> preguntas = encuesta.getPreguntas();

        for (int p = 0; p < usuarioA.length; p++) {
            if (p == preguntaExcluida) continue;
            if (usuarioA[p] != null && usuarioB[p] != null) {
                Pregunta pregunta = preguntas.get(p);
                sumaDistancias += calcularDistanciaUnaPregunta(usuarioA[p], usuarioB[p], pregunta);
                preguntasValidas++;
            }
        }

        return preguntasValidas > 0 ? sumaDistancias / preguntasValidas : Double.MAX_VALUE;
    }

    private double calcularDistanciaUnaPregunta(Object respA, Object respB, Pregunta pregunta) {
        TipoPregunta tipo = pregunta.getTipoPregunta();

        switch (tipo) {
            case NUMERICA:
                Numerica numerica = (Numerica) pregunta;
                double valA = ((Number) respA).doubleValue();
                double valB = ((Number) respB).doubleValue();
                double min = numerica.getMin();
                double max = numerica.getMax();
                return (max == min) ? 0.0 : Math.abs(valA - valB) / (max - min);

            case ORDINAL:
                Ordinal ordinal = (Ordinal) pregunta;
                List<String> opciones = new ArrayList<>(ordinal.getOpciones());
                int posA = opciones.indexOf(respA.toString());
                int posB = opciones.indexOf(respB.toString());
                return (opciones.size() <= 1) ? 0.0 : Math.abs(posA - posB) / (double) (opciones.size() - 1);

            case CATEGORIA_SIMPLE:
                return respA.equals(respB) ? 0.0 : 1.0;

            case CATEGORIA_MULTIPLE:
                Set<String> setA = convertToSet(respA);
                Set<String> setB = convertToSet(respB);
                Set<String> common = new HashSet<>(setA);
                common.retainAll(setB);
                Set<String> all = new HashSet<>(setA);
                all.addAll(setB);
                return all.isEmpty() ? 0.0 : 1.0 - (common.size() / (double) all.size());

            case LIBRE:
                String a = respA.toString();
                String b = respB.toString();
                int lev = levenshteinDistance(a, b);
                int maxLen = Math.max(a.length(), b.length());
                int absDiff = Math.abs(a.length() - b.length());
                double denom = maxLen - absDiff;
                return (denom == 0) ? 0.0 : (lev - absDiff) / denom;

            default:
                return 1.0;
        }
    }

    private Object calcularValorImputado(List<VecinoDistancia> vecinos,
                                         int preguntaIdx, Encuesta encuesta) {
        Pregunta pregunta = encuesta.getPreguntas().get(preguntaIdx);
        TipoPregunta tipo = pregunta.getTipoPregunta();

        switch (tipo) {
            case NUMERICA:
                double sumaValores = 0.0;
                double sumaPesos = 0.0;
                for (VecinoDistancia v : vecinos) {
                    double peso = 1.0 / (v.distancia + 0.0001);
                    sumaValores += ((Number) v.valor).doubleValue() * peso;
                    sumaPesos += peso;
                }
                return sumaValores / sumaPesos;

            case ORDINAL:
            case CATEGORIA_SIMPLE:
            case LIBRE:
                Map<Object, Integer> frecuencias = new HashMap<>();
                for (VecinoDistancia v : vecinos) {
                    frecuencias.put(v.valor, frecuencias.getOrDefault(v.valor, 0) + 1);
                }
                return Collections.max(frecuencias.entrySet(), Map.Entry.comparingByValue()).getKey();

            case CATEGORIA_MULTIPLE:
                Map<String, Integer> opcionesFrecuencia = new HashMap<>();
                for (VecinoDistancia v : vecinos) {
                    Set<String> opciones = convertToSet(v.valor);
                    for (String opcion : opciones) {
                        opcionesFrecuencia.put(opcion, opcionesFrecuencia.getOrDefault(opcion, 0) + 1);
                    }
                }
                Set<String> resultado = new HashSet<>();
                int umbral = vecinos.size() / 2;
                for (Map.Entry<String, Integer> entry : opcionesFrecuencia.entrySet()) {
                    if (entry.getValue() > umbral) {
                        resultado.add(entry.getKey());
                    }
                }
                return resultado.isEmpty() ? new HashSet<>() : resultado;

            default:
                return obtenerValorPorDefecto(preguntaIdx, encuesta);
        }
    }

    private Object obtenerValorPorDefecto(int preguntaIdx, Encuesta encuesta) {
        Pregunta pregunta = encuesta.getPreguntas().get(preguntaIdx);
        TipoPregunta tipo = pregunta.getTipoPregunta();

        switch (tipo) {
            case NUMERICA:
                Numerica numerica = (Numerica) pregunta;
                return (numerica.getMin() + numerica.getMax()) / 2.0;
            case ORDINAL:
                Ordinal ordinal = (Ordinal) pregunta;
                List<String> opciones = new ArrayList<>(ordinal.getOpciones());
                return opciones.isEmpty() ? "" : opciones.get(opciones.size() / 2);
            case CATEGORIA_SIMPLE:
            case LIBRE:
                return "";
            case CATEGORIA_MULTIPLE:
                return new HashSet<String>();
            default:
                return null;
        }
    }

    private Set<String> convertToSet(Object obj) {
        if (obj instanceof Set) {
            return (Set<String>) obj;
        } else if (obj instanceof Collection) {
            return new HashSet<>((Collection<String>) obj);
        } else if (obj instanceof String) {
            Set<String> set = new HashSet<>();
            set.add((String) obj);
            return set;
        }
        return new HashSet<>();
    }

    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],
                            Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }

        return dp[a.length()][b.length()];
    }

    private Object[] convertirRespuestasAArray(
            UsuarioRespondedor usuario,
            Encuesta encuesta,
            IndicePregunta indicePregunta
    ) {
        List<Respuesta> respuestas = usuario.getRespuestasEncuesta(encuesta.getId());
        List<Pregunta> preguntas = encuesta.getPreguntas();

        Object[] array = new Object[preguntas.size()];
        Arrays.fill(array, null);

        for (Respuesta r : respuestas) {
            int indice = indicePregunta.obtenerIndice(encuesta.getId(), r.getIdPregunta());
            if (indice >= 0 && indice < array.length) {
                array[indice] = r.getValor();
            }
        }

        return array;
    }

    private void configurarEstrategia(AlgoritmoClustering estrategia, Encuesta encuesta) {
        List<Pregunta> preguntas = encuesta.getPreguntas();

        for (int i = 0; i < preguntas.size(); i++) {
            Pregunta p = preguntas.get(i);

            if (p instanceof Numerica) {
                Numerica n = (Numerica) p;
                estrategia.setNumericRange(i, n.getMin(), n.getMax());
            } else if (p instanceof Ordinal) {
                Ordinal o = (Ordinal) p;
                estrategia.setOrdinalOptions(i, o.getOpciones());
            }
        }
    }

    private TipoPregunta[] extraerTiposPreguntas(Encuesta encuesta) {
        List<Pregunta> preguntas = encuesta.getPreguntas();
        TipoPregunta[] tipos = new TipoPregunta[preguntas.size()];

        for (int i = 0; i < preguntas.size(); i++) {
            tipos[i] = preguntas.get(i).getTipoPregunta();
        }

        return tipos;
    }

    private static class VecinoDistancia {
        int indice;
        double distancia;
        Object valor;

        VecinoDistancia(int indice, double distancia, Object valor) {
            this.indice = indice;
            this.distancia = distancia;
            this.valor = valor;
        }
    }
}

