package main.domain.classes;

import main.domain.controllers.CtrlDominio;

/**
 * Adaptador que permite obtener el índice de una pregunta dentro de una encuesta.
 * <p>
 * Esta clase actúa como intermediario entre el dominio y el controlador de dominio,
 * delegando la obtención del índice de una pregunta a {@link CtrlDominio}.
 * <p>
 * Implementa la interfaz {@link IndicePregunta} para desacoplar el dominio de la
 * implementación concreta del controlador.
 */
public class AdaptadorPregunta implements IndicePregunta {

    private final CtrlDominio ctrlDominio;

    /**
     * Crea un adaptador de preguntas asociado a un controlador de dominio.
     *
     * @param ctrlDominio controlador de dominio utilizado para resolver los índices
     */
    public AdaptadorPregunta(CtrlDominio ctrlDominio) {
        this.ctrlDominio = ctrlDominio;
    }

    /**
     * Obtiene el índice de una pregunta dentro de una encuesta concreta.
     *
     * @param idEncuesta identificador de la encuesta
     * @param idPregunta identificador de la pregunta
     * @return índice de la pregunta dentro de la encuesta
     */
    @Override
    public int obtenerIndice(String idEncuesta, String idPregunta) {
        return ctrlDominio.obtenerIndicePregunta(idEncuesta, idPregunta);
    }
}
