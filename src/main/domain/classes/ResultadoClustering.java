package main.domain.classes;

import java.util.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Representa el resultado de un proceso de clustering aplicado sobre usuarios
 * de una encuesta.
 * <p>
 * Este objeto encapsula tanto los resultados principales del clustering
 * (asignación de grupos, centros, métricas) como la información necesaria
 * para relacionarlos con los usuarios y la encuesta correspondiente.
 * <p>
 * Actúa como un objeto de transferencia de datos (DTO) entre la capa de
 * algoritmos de clustering y el resto del sistema.
 */
public class ResultadoClustering {

    /**
     * Grupo asignado a cada usuario, en el mismo orden que {@code idsUsuarios}.
     */
    public int[] groups;

    /**
     * Centros representativos de cada grupo.
     */
    public Object[][] centers;

    /**
     * Valor de la métrica silhouette del clustering.
     */
    public double silhouette;

    /**
     * Identificadores de los usuarios, en el mismo orden que {@code groups}.
     */
    public List<String> idsUsuarios;

    // Metadata del clustering
    private final String algoritmo;      // "KMedoids", "KMeans", etc.
    private final int k;                  // número de clusters
    private final int numIteraciones;     // iteraciones ejecutadas
    private final double inercia;
    private String idEncuesta;

    /**
     * Crea un resultado de clustering con la información principal del algoritmo.
     *
     * @param groups          grupo asignado a cada usuario
     * @param centers         centros de los clusters
     * @param silhouette      valor de la métrica silhouette
     * @param algoritmo       nombre del algoritmo utilizado
     * @param k               número de clusters
     * @param numIteraciones  número de iteraciones ejecutadas
     * @param inercia         valor de la inercia final
     */
    public ResultadoClustering(int[] groups, Object[][] centers, double silhouette,
                               String algoritmo, int k, int numIteraciones,
                               double inercia) {
        this.groups = groups;
        this.centers = centers;
        this.silhouette = silhouette;
        this.algoritmo = algoritmo;
        this.k = k;
        this.inercia = inercia;
        this.numIteraciones = numIteraciones;
    }

    /**
     * Devuelve la asignación de grupos de los usuarios.
     *
     * @return array de grupos
     */
    public int[] getGroups() {
        return groups;
    }

    /**
     * Devuelve los centros de los clusters.
     *
     * @return matriz de centros
     */
    public Object[][] getCenters() {
        return centers;
    }

    /**
     * Devuelve el valor de la métrica silhouette.
     *
     * @return valor silhouette
     */
    public double getSilhouette() {
        return silhouette;
    }

    /**
     * Devuelve el nombre del algoritmo de clustering utilizado.
     *
     * @return nombre del algoritmo
     */
    public String getAlgoritmo() {
        return algoritmo;
    }

    /**
     * Devuelve el número de clusters utilizados.
     *
     * @return número de clusters
     */
    public int getK() {
        return k;
    }

    /**
     * Devuelve el número de iteraciones ejecutadas por el algoritmo.
     *
     * @return número de iteraciones
     */
    public int getNumIteraciones() {
        return numIteraciones;
    }

    /**
     * Devuelve el valor de la inercia final del clustering.
     *
     * @return valor de la inercia
     */
    public double getInercia() {
        return inercia;
    }

    /**
     * Devuelve el identificador de la encuesta asociada al clustering.
     *
     * @return identificador de la encuesta
     */
    public String getIdEncuesta() {
        return idEncuesta;
    }

    /**
     * Asocia el resultado del clustering a una encuesta concreta.
     *
     * @param idEncuesta identificador de la encuesta
     */
    public void setIdEncuesta(String idEncuesta) {
        this.idEncuesta = idEncuesta;
    }

    /**
     * Establece la lista de identificadores de usuarios en el mismo orden
     * que la asignación de grupos.
     *
     * @param idsUsuarios lista de identificadores de usuarios
     */
    public void setIdsUsuarios(List<String> idsUsuarios) {
        this.idsUsuarios = idsUsuarios;
    }

    /**
     * Agrupa los identificadores de usuarios por cluster.
     *
     * @return lista de listas, donde cada sublista contiene los IDs de los
     *         usuarios pertenecientes a un mismo grupo
     */
    public List<List<String>> getUsuariosPorGrupo() {
        List<List<String>> usuariosPorGrupo = new ArrayList<>();

        // Inicializar listas para cada grupo
        for (int g = 0; g < k; g++) {
            usuariosPorGrupo.add(new ArrayList<>());
        }

        // Asignar IDs de usuarios a sus grupos correspondientes
        for (int i = 0; i < groups.length; i++) {
            int grupo = groups[i];
            String idUsuario = idsUsuarios.get(i);
            usuariosPorGrupo.get(grupo).add(idUsuario);
        }

        return usuariosPorGrupo;
    }

    /**
     * Devuelve la lista de identificadores de usuarios.
     *
     * @return lista de IDs de usuarios
     */
    public List<String> getIdsUsuarios() {
        return idsUsuarios;
    }
}
