package main.domain.classes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del algoritmo de clustering K-Means.
 * <p>
 * Agrupa instancias (por ejemplo, usuarios) en {@code k} clusters, calculando
 * centros (centroides) en forma de vectores {@code Object[]} en función del tipo
 * de cada pregunta ({@link TipoPregunta}).
 * <p>
 * La distancia entre instancias se calcula combinando distancias parciales por pregunta:
 * <ul>
 *   <li>{@link TipoPregunta#NUMERICA}: distancia normalizada por rango.</li>
 *   <li>{@link TipoPregunta#ORDINAL}: distancia por posición en opciones.</li>
 *   <li>{@link TipoPregunta#CATEGORIA_SIMPLE}: 0/1 según igualdad.</li>
 *   <li>{@link TipoPregunta#CATEGORIA_MULTIPLE}: 1 - Jaccard.</li>
 *   <li>{@link TipoPregunta#LIBRE}: distancia basada en Levenshtein normalizada.</li>
 * </ul>
 * <p>
 * Los centros se recalculan usando media (numérica) o moda (categórica/ordinal/libre),
 * y para multiselección se calcula un conjunto de opciones más frecuentes.
 */
public class KMeans implements AlgoritmoClustering {

    private final int k;
    private final int maxIter;
    private TipoPregunta[] questionTypes;
    private final Map<Integer, Double> minValues;
    private final Map<Integer, Double> maxValues;
    private final Map<Integer, Set<String>> ordinalOptions;
    private Random random;

    /**
     * Crea una instancia del algoritmo K-Means.
     *
     * @param k       número de clusters a formar
     * @param maxIter número máximo de iteraciones
     */
    public KMeans(int k, int maxIter) {
        this.k = k;
        this.maxIter = maxIter;
        this.minValues = new HashMap<>();
        this.maxValues = new HashMap<>();
        this.ordinalOptions = new HashMap<>();
        this.random = new Random();
    }

    // === Configuración ===

    /**
     * Establece los tipos de pregunta usados para el cálculo de distancias
     * y el recalculado de centros.
     *
     * @param tipos array de tipos de pregunta, alineado con las columnas del dataset
     */
    @Override
    public void setTipoPreguntas(TipoPregunta[] tipos){
        this.questionTypes = tipos;
    }

    /**
     * Configura el rango numérico asociado a una pregunta por índice.
     *
     * @param questionIndex índice de la pregunta
     * @param min           mínimo del rango
     * @param max           máximo del rango
     */
    @Override
    public void setNumericRange(int questionIndex, double min, double max) {
        minValues.put(questionIndex, min);
        maxValues.put(questionIndex, max);
    }

    /**
     * Configura las opciones válidas para una pregunta ordinal por índice.
     *
     * @param questionIndex índice de la pregunta ordinal
     * @param options       conjunto de opciones válidas
     */
    @Override
    public void setOrdinalOptions(int questionIndex, Set<String> options) {
        ordinalOptions.put(questionIndex, new HashSet<>(options));
    }

    /**
     * Ejecuta el algoritmo K-Means sobre el dataset proporcionado.
     *
     * @param data matriz de datos: cada fila es una instancia y cada columna una pregunta
     * @return resultado del clustering (asignación de grupos, centros y métricas)
     * @throws IllegalArgumentException si los datos son inválidos o {@code k} no es válido
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

        final int numQuestions = data[0].length;

        // 1) Inicialización de centros (k muestras distintas)
        Object[][] centers = initializeCenters(data);

        // 2) Vector de asignaciones
        int[] groups = new int[n];
        Arrays.fill(groups, -1);

        boolean changed = true;
        int iter = 0;

        // 3) Bucle principal
        while (changed && iter < maxIter) {
            changed = false;

            // 3.1) Asignar cada punto al centro más cercano
            for (int i = 0; i < n; i++) {
                double best = Double.MAX_VALUE;
                int bestGroup = -1;
                for (int c = 0; c < k; c++) {
                    double d = calculateDistance(data[i], centers[c]);
                    if (d < best) {
                        best = d;
                        bestGroup = c;
                    }
                }
                if (groups[i] != bestGroup) {
                    groups[i] = bestGroup;
                    changed = true;
                }
            }

            // 3.2) Recalcular centros (media/moda según tipo)
            centers = updateCenters(data, groups, numQuestions, centers);

            iter++;
        }

        // 4) Silhouette y inercia
        double silhouette = calculateSilhouette(data, groups);
        double inercia = calcularInercia(data, groups, centers);

        // 5) Empaquetar
        return new ResultadoClustering(
            groups,
            centers,
            silhouette,
            "KMeans",
            k,
            iter,
            inercia
        );
    }

    /**
     * Establece la semilla del generador aleatorio usado durante la inicialización/re-seeding.
     *
     * @param seed semilla a utilizar
     */
    public void setSeed(long seed) {
        this.random = new Random(seed);
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

    // === Inicialización: elegir k índices distintos y copiar las filas ===
    private Object[][] initializeCenters(Object[][] data) {
        int n = data.length;
        Object[][] centers = new Object[k][];
        Set<Integer> chosen = new HashSet<>();
        int placed = 0;
        while (placed < k) {
            int r = random.nextInt(n);
            if (chosen.add(r)) {
                centers[placed] = Arrays.copyOf(data[r], data[r].length);
                placed++;
            }
        }
        return centers;
    }

    // === Distancia ===
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
                    int levenshtein= levenshtein(a, b);
                    int max_ft = Math.max(a.length(), b.length());
                    int absDiff= Math.abs(a.length() - b.length());
                    double denom = max_ft - absDiff;
                    if(denom == 0) questionDistance = 0.0;
                    else questionDistance = (levenshtein - absDiff) / denom;
                    break;
            }

            totalDistance += questionDistance;
        }

        return totalDistance / (double) numQuestions;
    }

    // === Re-cálculo de centros por tipo ===
    private Object[][] updateCenters(Object[][] data, int[] groups, int numQuestions, Object[][] currentCenters) {
        if (questionTypes == null) {
            throw new IllegalStateException("questionTypes no está inicializado. Llama antes a setTipoPreguntas().");
        }

        Object[][] newCenters = new Object[k][numQuestions];

        List<List<Object[]>> buckets = new ArrayList<>(k);
        for (int c = 0; c < k; c++) buckets.add(new ArrayList<>());
        for (int i = 0; i < data.length; i++) buckets.get(groups[i]).add(data[i]);

        for (int c = 0; c < k; c++) {
            List<Object[]> cluster = buckets.get(c);
            if (cluster.isEmpty()) {
                if (currentCenters != null && currentCenters[c] != null) {
                    newCenters[c] = Arrays.copyOf(currentCenters[c], numQuestions);
                }
                // re-seed aleatorio si un cluster queda vacío
                int r = random.nextInt(data.length);
                newCenters[c] = Arrays.copyOf(data[r], numQuestions);
                continue;
            }
            Object[] center = new Object[numQuestions];

            for (int p = 0; p < numQuestions; p++) {
                switch (questionTypes[p]) {
                    case NUMERICA: {
                        double sum = 0.0;
                        for (Object[] row : cluster) sum += ((Number) row[p]).doubleValue();
                        center[p] = sum / cluster.size();
                        break;
                    }
                    case ORDINAL:
                    case CATEGORIA_SIMPLE: {
                        center[p] = mode(cluster, p);
                        break;
                    }
                    case CATEGORIA_MULTIPLE: {
                        center[p] = modeMultiple(cluster, p);
                        break;
                    }
                    case LIBRE: {
                        // centro: la moda del texto
                        center[p] = mode(cluster, p);
                        break;
                    }
                }
            }

            newCenters[c] = center;
        }

        return newCenters;
    }

    /**
     * Calcula la métrica silhouette promedio para la asignación de clusters obtenida.
     *
     * @param data   dataset utilizado (instancias por filas)
     * @param groups asignación de cluster por instancia
     * @return valor silhouette promedio; si no es computable, devuelve {@code 0.0}
     */
    public double calculateSilhouette(Object[][] data, int[] groups) {
        int n = data.length;
        if (n <= 1) return 0.0;

        // Tamaño de cada cluster
        int[] size = new int[k];
        for (int g : groups) size[g]++;

        double sSum = 0.0;

        for (int i = 0; i < n; i++) {
            int ci = groups[i];

            // --- a(i): media a su propio cluster ---
            double a = 0.0;
            if (size[ci] > 1) {
                for (int j = 0; j < n; j++) {
                    if (j != i && groups[j] == ci) {
                        a += calculateDistance(data[i], data[j]);
                    }
                }
                a /= (size[ci] - 1);
            } else {
                a = 0.0; // punto solo en su cluster → silhouette 0
            }

            // --- b(i): mínima distancia media a cualquier otro cluster ---
            double[] distC = new double[k]; // suma de distancias a cada cluster
            // rellenamos distC en una sola pasada
            for (int j = 0; j < n; j++) {
                int cj = groups[j];
                if (cj != ci) {
                    distC[cj] += calculateDistance(data[i], data[j]);
                }
            }

            double b = Double.POSITIVE_INFINITY;
            for (int c = 0; c < k; c++) {
                if (c == ci || size[c] == 0) continue; // ignorar propio cluster y vacíos
                double avg = distC[c] / size[c];       // media a cluster c
                if (avg < b) b = avg;
            }
            if (Double.isInfinite(b)) b = 0.0; // por seguridad

            double s;
            if (a == 0.0 && b == 0.0) s = 0.0;
            else s = (b - a) / Math.max(a, b);

            sSum += s;
        }

        return sSum / n;
    }

    // === Helpers ===

    private Set<String> convertToSet(Object response) {
        if (response == null) return Collections.emptySet();
        if (response instanceof Set) {
            return (Set<String>) response;
        } else if (response instanceof String) {
            String str = (String) response;
            if (str.isBlank()) return Collections.emptySet();
            return Arrays.stream(str.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
        } else if (response instanceof String[]) {
            return Arrays.stream((String[]) response)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private int levenshtein(String s, String t) {
        int n = s.length();
        int m = t.length();

        int[][] dp = new int[n + 1][m + 1];

        // Coste de convertir prefijo de longitud i en cadena vacía: borrar i chars
        for (int i = 0; i <= n; i++) {
            dp[i][0] = i;
        }
        // Coste de convertir cadena vacía en prefijo de longitud j: insertar j chars
        for (int j = 0; j <= m; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int cost = (s.charAt(i - 1) == t.charAt(j - 1)) ? 0 : 1;

                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1,      // borrado
                                dp[i][j - 1] + 1),     // inserción
                        dp[i - 1][j - 1] + cost         // sustitución / copia
                );
            }
        }
        return dp[n][m];
    }

    private Object mode(List<Object[]> rows, int col) {
        Map<Object, Integer> count = new HashMap<>();
        for (Object[] r : rows) {
            Object v = r[col];
            count.put(v, count.getOrDefault(v, 0) + 1);
        }
        return count.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private Set<String> modeMultiple(List<Object[]> rows, int col) {
        Map<String, Integer> freq = new HashMap<>();

        for (Object[] r : rows) {
            for (String e : convertToSet(r[col])) {
                freq.put(e, freq.getOrDefault(e, 0) + 1);
            }
        }

        if (freq.isEmpty()) return Collections.emptySet();

        int maxFreq = Collections.max(freq.values());

        Set<String> res = new HashSet<>();
        for (Map.Entry<String, Integer> e : freq.entrySet()) {
            if (e.getValue() == maxFreq) {
                res.add(e.getKey());  // top-1 (o top-ties)
            }
        }
        return res;
    }
}
