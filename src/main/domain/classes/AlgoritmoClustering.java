package main.domain.classes;

import java.util.*;

/**
 * Interfaz que define el contrato de los algoritmos de clustering del sistema.
 * <p>
 * Esta interfaz sigue el patrón de diseño <b>Strategy</b>, permitiendo intercambiar
 * dinámicamente distintas implementaciones de algoritmos de clustering
 * (por ejemplo, K-Medoids, K-Means, etc.) sin modificar el código cliente.
 * <p>
 * Las implementaciones deben permitir configurar el tipo de cada pregunta y
 * los parámetros necesarios para el cálculo de distancias antes de ejecutar
 * el algoritmo.
 */
public interface AlgoritmoClustering {

    /**
     * Ejecuta el algoritmo de clustering sobre un conjunto de datos.
     * <p>
     * Cada fila del dataset representa una instancia (por ejemplo, un usuario)
     * y cada columna corresponde a una pregunta.
     *
     * @param data matriz de datos a agrupar
     * @return resultado del clustering con la asignación de grupos y métricas asociadas
     */
    ResultadoClustering execute(Object[][] data);

    /**
     * Establece los tipos de pregunta asociados a cada columna del dataset.
     * <p>
     * El índice del array corresponde al índice de la pregunta dentro de cada fila.
     *
     * @param tipos array de tipos de pregunta
     */
    void setTipoPreguntas(TipoPregunta[] tipos);

    /**
     * Configura el rango numérico válido para una pregunta concreta.
     *
     * @param questionIndex índice de la pregunta numérica
     * @param min           valor mínimo permitido
     * @param max           valor máximo permitido
     */
    void setNumericRange(int questionIndex, double min, double max);

    /**
     * Configura las opciones válidas para una pregunta ordinal.
     *
     * @param questionIndex índice de la pregunta ordinal
     * @param options       conjunto de opciones válidas
     */
    void setOrdinalOptions(int questionIndex, Set<String> options);
}
// INTERFAZ PARA PATRÓN STRATEGY
