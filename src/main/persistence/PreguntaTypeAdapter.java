package main.persistence;

import com.google.gson.*;
import main.domain.classes.*;
import java.lang.reflect.Type;
import java.util.*;

/**
 * {@code PreguntaTypeAdapter} es un adaptador de Gson para serializar y deserializar
 * instancias polimórficas de {@link Pregunta}.
 * <p>
 * Problema que resuelve:
 * <ul>
 *   <li>{@link Pregunta} es abstracta y tiene múltiples subclases concretas</li>
 *   <li>Gson por defecto no preserva el tipo concreto al serializar un campo declarado como {@code Pregunta}</li>
 * </ul>
 * <p>
 * Solución:
 * <ul>
 *   <li>En {@link #serialize(Pregunta, Type, JsonSerializationContext)} se añade un discriminador de tipo
 *       {@code "tipoPregunta"} y se serializan explícitamente los campos comunes y específicos.</li>
 *   <li>En {@link #deserialize(JsonElement, Type, JsonDeserializationContext)} se lee {@code "tipoPregunta"},
 *       se instancia la subclase correspondiente y se restauran los campos comunes (incluyendo {@code id} y {@code obligatoria}).</li>
 * </ul>
 * <p>
 * Formato JSON generado (conceptualmente):
 * <pre>
 * {
 *   "tipoPregunta": "NUMERICA",
 *   "id": "...",
 *   "enunciado": "...",
 *   "obligatoria": false,
 *   "min": 0.0,
 *   "max": 10.0
 * }
 * </pre>
 * <p>
 * Nota: este adaptador depende de que {@link Pregunta} tenga setters para restaurar estado
 * persistido ({@link Pregunta#setId(String)} y {@link Pregunta#setObligatoria(boolean)}).
 */
public class PreguntaTypeAdapter implements JsonSerializer<Pregunta>, JsonDeserializer<Pregunta> {

    /**
     * Serializa una {@link Pregunta} a un {@link JsonElement} (concretamente un {@link JsonObject})
     * incluyendo:
     * <ul>
     *   <li>Discriminador {@code "tipoPregunta"} para permitir reconstrucción polimórfica</li>
     *   <li>Campos comunes: {@code id}, {@code enunciado}, {@code obligatoria}</li>
     *   <li>Campos específicos según {@link TipoPregunta}</li>
     * </ul>
     *
     * @param src        instancia de {@link Pregunta} a serializar
     * @param typeOfSrc  tipo declarado del origen (normalmente {@code Pregunta})
     * @param context    contexto de serialización de Gson para serializar estructuras auxiliares
     * @return un {@link JsonElement} representando la pregunta
     */
    @Override
    public JsonElement serialize(Pregunta src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();

        // Agregar tipo de pregunta para deserialización
        obj.addProperty("tipoPregunta", src.getTipoPregunta().name());

        // Campos comunes
        obj.addProperty("id", src.getId());
        obj.addProperty("enunciado", src.getEnunciado());
        obj.addProperty("obligatoria", src.esObligatoria());

        // Campos específicos según el tipo
        switch (src.getTipoPregunta()) {
            case NUMERICA:
                Numerica num = (Numerica) src;
                if (num.getMin() != null) obj.addProperty("min", num.getMin());
                if (num.getMax() != null) obj.addProperty("max", num.getMax());
                break;

            case CATEGORIA_SIMPLE:
                CategoriaSimple cs = (CategoriaSimple) src;
                obj.add("opciones", context.serialize(cs.getOpciones()));
                break;

            case CATEGORIA_MULTIPLE:
                CategoriaMultiple cm = (CategoriaMultiple) src;
                obj.add("opciones", context.serialize(cm.getOpciones()));
                obj.addProperty("maxSelecciones", cm.getMaxSelecciones());
                break;

            case ORDINAL:
                Ordinal ord = (Ordinal) src;
                obj.add("opciones", context.serialize(ord.getOpciones()));
                break;

            case LIBRE:
                Libre lib = (Libre) src;
                obj.addProperty("longitudMaxima", lib.getLongitudMaxima());
                break;
        }

        return obj;
    }

    /**
     * Deserializa una {@link Pregunta} desde un {@link JsonElement}, leyendo primero el discriminador
     * {@code "tipoPregunta"} para decidir la subclase concreta.
     * <p>
     * Flujo:
     * <ol>
     *   <li>Leer {@code tipoPregunta} y convertirlo a {@link TipoPregunta}</li>
     *   <li>Leer campos comunes: {@code id}, {@code enunciado}, {@code obligatoria}</li>
     *   <li>Instanciar la subclase correspondiente usando los campos específicos</li>
     *   <li>Restaurar {@code id} y {@code obligatoria} mediante setters</li>
     * </ol>
     *
     * @param json      elemento JSON que representa una pregunta
     * @param typeOfT   tipo destino declarado (normalmente {@code Pregunta})
     * @param context   contexto de deserialización de Gson para colecciones/estructuras auxiliares
     * @return instancia concreta de {@link Pregunta}
     * @throws JsonParseException si el tipo es desconocido o el JSON no tiene el formato esperado
     */
    @Override
    public Pregunta deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject obj = json.getAsJsonObject();

        // Leer tipo de pregunta
        String tipoStr = obj.get("tipoPregunta").getAsString();
        TipoPregunta tipo = TipoPregunta.valueOf(tipoStr);

        // Leer campos comunes
        String id = obj.get("id").getAsString();
        String enunciado = obj.get("enunciado").getAsString();
        boolean obligatoria = obj.get("obligatoria").getAsBoolean();

        // Crear pregunta según el tipo
        Pregunta pregunta;

        switch (tipo) {
            case NUMERICA:
                Double min = obj.has("min") ? obj.get("min").getAsDouble() : null;
                Double max = obj.has("max") ? obj.get("max").getAsDouble() : null;
                pregunta = new Numerica(enunciado, min, max);
                break;

            case CATEGORIA_SIMPLE:
                Set<String> opcionesCS = context.deserialize(
                        obj.get("opciones"),
                        new com.google.gson.reflect.TypeToken<Set<String>>(){}.getType()
                );
                pregunta = new CategoriaSimple(enunciado, opcionesCS);
                break;

            case CATEGORIA_MULTIPLE:
                Set<String> opcionesCM = context.deserialize(
                        obj.get("opciones"),
                        new com.google.gson.reflect.TypeToken<Set<String>>(){}.getType()
                );
                int maxSelecciones = obj.get("maxSelecciones").getAsInt();
                pregunta = new CategoriaMultiple(enunciado, opcionesCM, maxSelecciones);
                break;

            case ORDINAL:
                Set<String> opcionesOrd = context.deserialize(
                        obj.get("opciones"),
                        new com.google.gson.reflect.TypeToken<Set<String>>(){}.getType()
                );
                pregunta = new Ordinal(enunciado, opcionesOrd);
                break;

            case LIBRE:
                int longitudMaxima = obj.has("longitudMaxima") ?
                        obj.get("longitudMaxima").getAsInt() : 1000;
                pregunta = new Libre(enunciado, longitudMaxima);
                break;

            default:
                throw new JsonParseException("Tipo de pregunta desconocido: " + tipo);
        }

        // Restaurar ID y obligatoria
        pregunta.setId(id);
        pregunta.setObligatoria(obligatoria);

        return pregunta;
    }
}
