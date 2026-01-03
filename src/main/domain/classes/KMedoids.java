package main.domain.classes;

import java.util.*;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Implementación del algoritmo de clustering K-Medoids.
 * <p>
 * Agrupa instancias (por ejemplo, usuarios) en {@code k} clusters eligiendo como
 * representantes (medoides) elementos reales del dataset.
 * <p>
 * La distancia entre instancias se calcula según los tipos de preguntas
 * ({@link TipoPregunta}) y la configuración asociada (rangos numéricos, opciones
 * ordinales, etc.).
 * <p>
 * Esta clase implementa el contrato {@link AlgoritmoClustering}.
 */
public class KMedoids implements AlgoritmoClustering {

    private int k;
    private final int maxIter;
    private TipoPregunta[] questionTypes;
    private final Map<Integer, Double> minValues;
    private final Map<Integer, Double> maxValues;
    private final Map<Integer, Set<String>> ordinalOptions;
    private Random random;

    /**
     * Crea una instancia del algoritmo K-Medoids.
     *
     * @param k       número de clusters a formar
     * @param maxIter número máximo de iteraciones permitidas
     */
    public KMedoids(int k, int maxIter) {
        this.k = k;
        this.maxIter = maxIter;
        this.minValues = new HashMap<>();
        this.maxValues = new HashMap<>();
        this.ordinalOptions = new HashMap<>();
        this.random = new Random();
    }

    /**
     * Establece los tipos de pregunta usados para el cálculo de distancias.
     * <p>
     * El índice del array corresponde al índice de la pregunta dentro de cada fila del dataset.
     *
     * @param tipos array de tipos de pregunta, alineado con las columnas del dataset
     */
    @Override
    public void setTipoPreguntas(TipoPregunta[] tipos){
        this.questionTypes = tipos;
    }

    /**
     * Configura el rango numérico asociado a una pregunta (por índice).
     *
     * @param questionIndex índice de la pregunta numérica dentro de la fila del dataset
     * @param min           mínimo del rango
     * @param max           máximo del rango
     */
    @Override
    public void setNumericRange(int questionIndex, double min, double max) {
        minValues.put(questionIndex, min);
        maxValues.put(questionIndex, max);
    }

    /**
     * Configura el conjunto de opciones válido para una pregunta ordinal (por índice).
     *
     * @param questionIndex índice de la pregunta ordinal dentro de la fila del dataset
     * @param options       opciones válidas de la pregunta ordinal
     */
    @Override
    public void setOrdinalOptions(int questionIndex, Set<String> options) {
        ordinalOptions.put(questionIndex, new HashSet<>(options));
    }

    /**
     * Ejecuta el algoritmo de clustering K-Medoids sobre el dataset proporcionado.
     *
     * @param data matriz de datos: cada fila representa una instancia y cada columna una pregunta
     * @return resultado del clustering (grupos, medoides, métricas y metadatos)
     * @throws IllegalArgumentException si los datos son nulos/vacíos o si {@code k} es inválido
     */
    @Override
    public ResultadoClustering execute(Object[][] data) {
        if (data == null) {
            throw new IllegalArgumentException("Los datos no pueden ser nulos");
        }
        int n = data.length;
        if (n == 0) {
            throw new IllegalArgumentException("El dataset no puede estar vacío");
        }

        if (k <= 0) {
            throw new IllegalArgumentException("k debe ser > 0");
        }

        if (k > n) {
            throw new IllegalArgumentException("k no puede ser mayor que el número de elementos (" + n + ")");
        }

        random.setSeed(0L);

        // PASO 1: Elegir k puntos usando KMedoids++ para mejores medoides iniciales

        int[] medoidIndices = kmedoidspp(data);

        // Array para guardar a qué grupo pertenece cada persona
        int[] groups = new int[n];
        Arrays.fill(groups, -1);

        boolean cambio = true;
        int iter = 0;

        while (cambio && iter < maxIter) {

            cambio = false;

            // PASO 2: Asignar cada persona al medoide más cercano
            for (int i = 0; i < n; i++) {
                double distanciaMinima = Double.MAX_VALUE;
                int grupoAsignado = -1;

                // Probar distancia a cada medoide
                for (int j = 0; j < k; j++) {
                    double distancia = calculateDistance(data[i], data[medoidIndices[j]]);
                    if (distancia < distanciaMinima) {
                        distanciaMinima = distancia;
                        grupoAsignado = j;
                    }
                }

                // Si cambió de grupo, marcar que hubo cambio
                if (groups[i] != grupoAsignado) {
                    groups[i] = grupoAsignado;
                    cambio = true;
                }
            }

            // PASO 3: Para cada cluster, encontrar el mejor medoide
            // (el punto que esté más cerca de todos los demás del cluster)
            int[] nuevosMedoides = encontrarMejoresMedoides(data, groups);

            // Verificar si los medoides cambiaron
            if (!Arrays.equals(medoidIndices, nuevosMedoides)) {

                cambio = true;
                medoidIndices = nuevosMedoides;
            }

            iter++;
        }

        // Extraer los objetos medoides finales
        Object[][] medoids = new Object[k][];
        for (int i = 0; i < k; i++) {
            medoids[i] = data[medoidIndices[i]];
        }

        // Calcular silhouette y inercia
        double S = CalculateSilhouette(data, groups);
        double inercia = calcularInercia(data, groups, medoids);

        return new ResultadoClustering(
            groups,
            medoids,
            S,
            "KMedoids",
            k,
            iter,
            inercia
        );
    }

    /**
     * Establece la semilla del generador aleatorio usado por el algoritmo.
     *
     * @param seed semilla a utilizar
     */
    public void setSeed(long seed) {
        this.random = new Random(seed);
    }

    // Para cada cluster, encuentra el punto que minimiza la distancia total
    // a todos los demás puntos del mismo cluster
    private int[] encontrarMejoresMedoides(Object[][] data, int[] groups) {
        int[] nuevosMedoides = new int[k];

        for (int cluster = 0; cluster < k; cluster++) {
            // Encontrar todos los miembros de este cluster
            List<Integer> miembros = new ArrayList<>();
            for (int i = 0; i < data.length; i++) {
                if (groups[i] == cluster) {
                    miembros.add(i);
                }
            }

            // Si no hay miembros, mantener un medoide aleatorio
            if (miembros.isEmpty()) {
                nuevosMedoides[cluster] = -1;
                continue;
            }

            // Probar cada miembro como posible medoide
            double mejorDistanciaTotal = Double.MAX_VALUE;
            int mejorMedoide = miembros.getFirst();

            for (int candidato : miembros) {
                // Calcular suma de distancias desde este candidato a todos los demás
                double distanciaTotal = 0.0;
                for (int otro : miembros) {
                    distanciaTotal += calculateDistance(data[candidato], data[otro]);
                }

                // Si es mejor que el actual, guardarlo
                if (distanciaTotal < mejorDistanciaTotal) {
                    mejorDistanciaTotal = distanciaTotal;
                    mejorMedoide = candidato;
                }
            }

            nuevosMedoides[cluster] = mejorMedoide;
        }

        //lo hago con un set para hacerlo eficiente
        Set<Integer> medoidesUsados = new HashSet<>();
        for(int m : nuevosMedoides){
            if(m >= 0) medoidesUsados.add(m);
        }

        // Ahora procesar los clusters vacíos
        for(int i=0; i<k ; i++){
            if(nuevosMedoides[i]==-1){
                int r;
                do {
                    r = random.nextInt(data.length);
                } while(medoidesUsados.contains(r));

                nuevosMedoides[i]=r;
                medoidesUsados.add(r);
            }
        }
        return nuevosMedoides;
    }

    // Calcula la distancia entre dos personas según sus respuestas
    private double calculateDistance(Object[] personA, Object[] personB) {
        double totalDistance = 0.0;
        int numQuestions = personA.length;

        for (int p = 0; p < numQuestions; p++) {
            Object responseA = personA[p];
            Object responseB = personB[p];
            double questionDistance = 0.0;

            switch (questionTypes[p]) {
                case NUMERICA:
                    double valueA = ((Number) responseA).doubleValue();
                    double valueB = ((Number) responseB).doubleValue();

                    if (!minValues.containsKey(p) || !maxValues.containsKey(p)) {
                        throw new IllegalStateException(
                            "No se configuraron minValues y maxValues para la pregunta numérica " + p
                        );
                    }

                    double min = minValues.get(p);
                    double max = maxValues.get(p);

                    if (max == min) {
                        questionDistance = 0.0;
                    } else {
                        questionDistance = Math.abs(valueA - valueB) / (max - min);
                    }
                    break;

                case ORDINAL:
                    List<String> optionsList = new ArrayList<>(ordinalOptions.get(p));
                    int posA = optionsList.indexOf(responseA.toString());
                    int posB = optionsList.indexOf(responseB.toString());
                    questionDistance = Math.abs(posA - posB) / (double)(optionsList.size() - 1);
                    break;

                case CATEGORIA_SIMPLE:
                    if(responseA.equals(responseB)){
                        questionDistance = 0.0;
                    } else {
                        questionDistance = 1.0;
                    }
                    break;

                case CATEGORIA_MULTIPLE:
                    Set<String> setA = convertToSet(responseA);
                    Set<String> setB = convertToSet(responseB);
                    Set<String> common = new HashSet<>(setA);
                    common.retainAll(setB);
                    Set<String> all = new HashSet<>(setA);
                    all.addAll(setB);
                    if(all.isEmpty()) questionDistance = 0.0;
                    else questionDistance = 1.0 - (common.size() / (double) all.size());
                    break;

                case LIBRE:
                    String a = responseA.toString();
                    String b = responseB.toString();
                    int lev= lev(a, b);
                    int max_ft = Math.max(a.length(), b.length());
                    int absDiff= Math.abs(a.length() - b.length());
                    double denom = max_ft - absDiff;
                    if(denom == 0) questionDistance = 0.0;
                    else questionDistance = (lev - absDiff) / denom;
                    break;
            }

            totalDistance += questionDistance;
        }

        return totalDistance / (double) numQuestions;
    }

    // === HELPERS ===

    private int findPosition(String[] options, String value) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }

    private double calcularInercia(Object[][] data, int[] groups, Object[][] centers) {
        double inercia = 0.0;

        for (int i = 0; i < data.length; i++) {
            int cluster = groups[i];
            double distancia = calculateDistance(data[i], centers[cluster]);
            inercia += distancia * distancia;
        }

        return inercia;
    }

    private Set<String> convertToSet(Object response) {
        if (response instanceof Set) {
            return (Set<String>) response;
        } else if (response instanceof String) {
            String str = (String) response;
            return Arrays.stream(str.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
        } else if (response instanceof String[]) {
            return Arrays.stream((String[]) response).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    // para hacerla eficiente la hago con programacion dinamica
    private int lev(String sa, String sb) {
        int a = sa.length();
        int b = sb.length();
        int[][] aux = new int[a + 1][b + 1];
        for (int i = 0; i <= a; i++) {
            aux[i][0] = i;
        }
        for (int j = 0; j <= b; j++) {
            aux[0][j] = j;
        }
        for (int i = 1; i <= a; i++) {
            for (int j = 1; j <= b; j++) {
                int cost = (sa.charAt(i - 1) == sb.charAt(j - 1)) ? 0 : 1;
                aux[i][j] = Math.min(Math.min(aux[i - 1][j] + 1, aux[i][j - 1] + 1), aux[i - 1][j - 1] + cost);
            }
        }
        return aux[a][b];
    }

    // me ha costado bastante hacer la implementacion esta jajaj
    /**
     * Calcula la métrica silhouette promedio del clustering resultante.
     *
     * @param data   dataset utilizado (instancias por filas)
     * @param groups asignación de cluster por instancia
     * @return valor silhouette promedio; si no es computable, devuelve {@code 0.0}
     */
    public double CalculateSilhouette(Object[][] data, int[] groups) {
        if (data == null || data.length <= 1 || k <= 1) {
            return 0.0;
        }

        double S=0.0;

        // lo calculamos al inicio una vez y nos ahorramos calcularlo todo el rato
        List<Integer> weightC = new ArrayList<>(Collections.nCopies(k, 0));
        for(int i = 0; i<data.length; i++) weightC.set(groups[i], weightC.get(groups[i]) + 1);

        //Empieza el algoritmo
        for(int i = 0; i<data.length; i++){

            // calcular ai

            int cluster = groups[i];
            double ai = 0.0;
            for(int j = 0; j<data.length; j++){

                if(groups[j] == cluster && i != j){
                    ai += calculateDistance(data[i], data[j]);
                }

            }
            if(weightC.get(cluster)-1 == 0) ai=0;
            else ai = ai / (double)(weightC.get(cluster)-1);

            //calcular bi
            List<Double> distC = new ArrayList<>(Collections.nCopies(k, 0.0));
            double bi = Double.MAX_VALUE;
            for(int j = 0; j<data.length; j++){
                if(groups[j] != cluster){
                    distC.set(groups[j], distC.get(groups[j]) + calculateDistance(data[i], data[j])); // con esto calculo la distancia media para cada grupo
                }
            }
            for(int j = 0; j<k; j++){
                if(j != cluster) distC.set(j , distC.get(j) / (double) (weightC.get(j)));
            }
            for(int j = 0; j<k; j++){
                if(j != cluster && bi>distC.get(j)) bi = distC.get(j);
            }

            // Le sumamos si a S directamente
            S += ((bi-ai) / Math.max(ai,bi));

        }

        S = S/data.length;
        return S;
    }

    private int[] kmedoidspp(Object[][] data){
        int n = data.length;
        int[] medoidIndices = new int[k];
        Set<Integer> selec = new HashSet<>();

        Integer r = random.nextInt(n);
        medoidIndices[0] = r;
        selec.add(r);

        for(int i=1; i<k; i++){
            double distTot = 0.0;
            double[] distM = new double[n];
            for(int j=0; j<n;j++)
            {
                if(!selec.contains(j)){

                    distM[j] = Double.MAX_VALUE;
                    for(int c=0; c<i; c++){
                        double d = calculateDistance(data[j],data[medoidIndices[c]]);
                        if(d<distM[j]) distM[j] = d;
                    }
                    // aqui ya es D(xj)
                    distM[j] = distM[j]*distM[j] ;
                    distTot+=(distM[j]);

                }
            }

            double rand = random.nextDouble() * distTot;
            double prob = 0.0;

            for (int j = 0; j < n; j++) {
                if (selec.contains(j)) continue;
                prob += distM[j];
                if (prob >= rand) {
                    medoidIndices[i] = j;
                    selec.add(j);
                    break;
                }
            }

        }
        return medoidIndices;
    }

}
