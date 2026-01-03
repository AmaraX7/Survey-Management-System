package main.domain.controllers;

import main.domain.classes.*;
import java.util.*;

/**
 * Controlador de lógica de negocio responsable de la ejecución de clustering.
 * <p>
 * Esta clase actúa como fachada de alto nivel para ejecutar algoritmos de clustering
 * sobre encuestas y usuarios respondedores, gestionando:
 * <ul>
 *   <li>La ejecución repetida de clustering para distintos valores de K</li>
 *   <li>La selección del mejor resultado según la métrica silhouette</li>
 *   <li>La creación y configuración de la estrategia de clustering adecuada</li>
 * </ul>
 * <p>
 * No interactúa con persistencia; todas las operaciones se realizan sobre objetos
 * del dominio en memoria.
 */
public class CtrlClustering {

    /**
     * Componente encargado de preparar datos y ejecutar los algoritmos de clustering.
     */
    private final Clustering clustering;

    /**
     * Construye el controlador de clustering.
     * <p>
     * Inicializa internamente una instancia de {@link Clustering}.
     */
    public CtrlClustering() {
        this.clustering = new Clustering();
    }

    // ========== EJECUCIÓN DE CLUSTERING ==========

    /**
     * Ejecuta clustering para múltiples valores de K.
     * <p>
     * Para cada valor de K en el rango [2, kMax], se ejecuta el algoritmo de clustering
     * indicado y se selecciona el mejor resultado para cada K según la métrica
     * silhouette.
     *
     * @param encuesta        encuesta sobre la que se realiza el clustering
     * @param usuarios        lista de usuarios respondedores
     * @param indicePregunta  estrategia para resolver el índice de preguntas
     * @param algoritmo       identificador del algoritmo ("KMEANS", "KMEANS++", "KMEDOIDS")
     * @param kMax            valor máximo de K a evaluar
     * @param maxIter         número máximo de iteraciones del algoritmo
     * @return lista de {@link ResultadoClustering}, uno por cada valor de K
     * @throws IllegalArgumentException si los parámetros son inválidos
     * @throws IllegalStateException    si no hay usuarios válidos
     */
    public List<ResultadoClustering> ejecutarClustering(
            Encuesta encuesta,
            List<UsuarioRespondedor> usuarios,
            IndicePregunta indicePregunta,
            String algoritmo,
            int kMax,
            int maxIter) {

        validarParametros(kMax, encuesta, usuarios);
        kMax = Math.min(kMax, usuarios.size());

        List<ResultadoClustering> resultados = new ArrayList<>();

        for (int k = 2; k <= kMax; k++) {
            ResultadoClustering mejorResultadoK = ejecutarParaK(
                    k, algoritmo, maxIter, usuarios, encuesta, indicePregunta);
            resultados.add(mejorResultadoK);
        }

        return resultados;
    }

    /**
     * Ejecuta clustering múltiples veces para un valor fijo de K y selecciona
     * el mejor resultado según la métrica silhouette.
     *
     * @param k               número de clusters
     * @param algoritmo       algoritmo a utilizar
     * @param maxIter         número máximo de iteraciones
     * @param usuarios        usuarios respondedores
     * @param encuesta        encuesta asociada
     * @param indicePregunta  estrategia para obtener el índice de preguntas
     * @return mejor {@link ResultadoClustering} obtenido para este valor de K
     */
    private ResultadoClustering ejecutarParaK(
            int k,
            String algoritmo,
            int maxIter,
            List<UsuarioRespondedor> usuarios,
            Encuesta encuesta,
            IndicePregunta indicePregunta) {

        ResultadoClustering mejorResultado = null;
        double mejorSilhouette = -2.0;
        int numEjecuciones = 10;

        for (int ejecucion = 0; ejecucion < numEjecuciones; ejecucion++) {
            AlgoritmoClustering estrategia = crearEstrategia(algoritmo, k, maxIter);
            clustering.configurarSemilla(estrategia, ejecucion * 1000L);

            ResultadoClustering resultado = clustering.ejecutarClustering(
                    estrategia, usuarios, encuesta, indicePregunta);

            if (resultado.getSilhouette() > mejorSilhouette) {
                mejorSilhouette = resultado.getSilhouette();
                mejorResultado = resultado;
            }
        }

        return mejorResultado;
    }

    /**
     * Selecciona el mejor resultado global según la métrica silhouette.
     *
     * @param resultados lista de resultados de clustering
     * @return resultado con mayor silhouette, o {@code null} si la lista es vacía
     */
    public ResultadoClustering encontrarMejorResultado(
            List<ResultadoClustering> resultados) {

        if (resultados == null || resultados.isEmpty()) {
            return null;
        }

        ResultadoClustering mejor = resultados.get(0);
        for (ResultadoClustering res : resultados) {
            if (res.getSilhouette() > mejor.getSilhouette()) {
                mejor = res;
            }
        }
        return mejor;
    }

    // ========== VALIDACIÓN Y UTILIDADES ==========

    /**
     * Valida los parámetros necesarios para ejecutar clustering.
     *
     * @param kMax     valor máximo de K
     * @param encuesta encuesta asociada
     * @param usuarios lista de usuarios
     * @throws IllegalArgumentException si K es menor que 2 o la encuesta es nula
     * @throws IllegalStateException    si no existen usuarios válidos
     */
    private void validarParametros(
            int kMax,
            Encuesta encuesta,
            List<UsuarioRespondedor> usuarios) {

        if (kMax < 2) {
            throw new IllegalArgumentException("K debe ser >= 2");
        }
        if (encuesta == null) {
            throw new IllegalArgumentException("Encuesta no puede ser null");
        }
        if (usuarios == null || usuarios.isEmpty()) {
            throw new IllegalStateException("No hay usuarios que hayan respondido");
        }
    }

    /**
     * Crea la estrategia de clustering correspondiente al identificador indicado.
     *
     * @param algoritmo identificador del algoritmo
     * @param k         número de clusters
     * @param maxIter   número máximo de iteraciones
     * @return implementación de {@link AlgoritmoClustering}
     * @throws IllegalArgumentException si el algoritmo no es reconocido
     */
    private AlgoritmoClustering crearEstrategia(String algoritmo, int k, int maxIter) {
        switch (algoritmo.toUpperCase()) {
            case "KMEANS":
            case "1":
                return new KMeans(k, maxIter);

            case "KMEANS++":
            case "2":
                return new KMeansPlusPlus(k, maxIter);

            case "KMEDOIDS":
            case "3":
                return new KMedoids(k, maxIter);

            default:
                throw new IllegalArgumentException("Algoritmo no reconocido: " + algoritmo);
        }
    }
}
