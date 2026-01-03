#!/bin/bash
# run.sh - Script para compilar y ejecutar la aplicación

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

show_help() {
    echo "Uso: ./run.sh [OPCIÓN]"
    echo ""
    echo "Opciones:"
    echo "  -h, --help          Mostrar esta ayuda"
    echo "  -c, --clean         Limpiar antes de compilar"
    echo "  -cd, --clean-data   Limpiar solo datos de persistencia"
    echo "  -r, --rebuild       Limpiar y compilar desde cero"
    echo "  -t, --test          Ejecutar tests en lugar de la app"
    echo ""
    echo "Sin opciones: compila y ejecuta la aplicación directamente"
}

# Procesar argumentos
CLEAN=false
CLEAN_DATA=false
RUN_TESTS=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -c|--clean)
            CLEAN=true
            shift
            ;;
        -cd|--clean-data)
            CLEAN_DATA=true
            shift
            ;;
        -r|--rebuild)
            CLEAN=true
            shift
            ;;
        -t|--test)
            RUN_TESTS=true
            shift
            ;;
        *)
            echo -e "${RED}Opción desconocida: $1${NC}"
            show_help
            exit 1
            ;;
    esac
done

# Limpiar si se solicitó
if [ "$CLEAN" = true ]; then
    echo -e "${YELLOW}=== Limpiando archivos compilados ===${NC}"
    make clean
fi

if [ "$CLEAN_DATA" = true ]; then
    echo -e "${YELLOW}=== Limpiando datos de persistencia ===${NC}"
    make clean-data
fi

# Ejecutar tests o aplicación
if [ "$RUN_TESTS" = true ]; then
    echo -e "${GREEN}=== Compilando y ejecutando tests ===${NC}"
    make test
else
    echo -e "${GREEN}=== Compilando y creando JAR ===${NC}"
    make jar-app

    if [ $? -eq 0 ]; then
        echo ""
        echo -e "${GREEN}=== Ejecutando aplicación ===${NC}"


        java --module-path "lib/javafx-sdk-21.0.9/lib" \
             --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base \
             -jar EXE/aplicacion.jar
    else
        echo -e "${RED}Error en la compilación${NC}"
        exit 1
    fi
fi
