# ==============================
# CONFIGURACIÓN BÁSICA
# ==============================
JAVAC = javac
JAVA = java
JAVADOC = javadoc


# Librerías
LIB_DIR = lib
GSON_JAR = $(LIB_DIR)/gson-2.10.1.jar
JAVAFX_LIB = $(LIB_DIR)/javafx-sdk-21.0.9/lib
JAVAFX_MODULES = javafx.controls,javafx.fxml,javafx.graphics,javafx.base

# JUnit
JUNIT_JAR = $(LIB_DIR)/junit-4.13.2.jar
HAMCREST_JAR = $(LIB_DIR)/hamcrest-core-1.3.jar

# SOURCE ROOTS (donde empiezan los paquetes)
SRC_ROOT_MAIN = src/main
SRC_ROOT_TEST = src/test
SRC_ROOT_DRIVERS = src/drivers

# Carpeta donde irán TODOS los .class
EXE = EXE

# PERSISTENCIA (runtime vs seed)
DATA_DIR = $(EXE)/data
SEED_DIR = $(EXE)/data_seed


# Classpath completo para compilación
CLASSPATH_COMPILE = $(EXE):$(GSON_JAR):$(JAVAFX_LIB)/*
CLASSPATH_MAIN = $(EXE):$(GSON_JAR):$(JAVAFX_LIB)/*
CLASSPATH_TESTS = $(EXE):$(GSON_JAR):$(JAVAFX_LIB)/*:$(JUNIT_JAR):$(HAMCREST_JAR)

# Fuentes Java
SRC_MAIN = $(shell find $(SRC_ROOT_MAIN) -name "*.java" 2>/dev/null)
SRC_TEST = $(shell find $(SRC_ROOT_TEST) -name "*.java" -not -path "*/test/test/*" 2>/dev/null)
SRC_DRIVERS = $(shell find $(SRC_ROOT_DRIVERS) -name "*.java" 2>/dev/null)

# ==============================
# AGRUPACIÓN DE TESTS
# ==============================
ALG_TESTS = \
    test.ClusteringTest \
    test.ResultadoClusteringTest \
    test.TestKMeans \
    test.TestKMeansPlusPlus \
    test.TestKMedoids

PREG_TESTS = \
    test.CategoriaMultipleTest \
    test.CategoriaSimpleTest \
    test.LibreTest \
    test.NumericaTest \
    test.OrdinalTest \
    test.RespuestaTest \
    test.EncuestaTest

OTHER_TESTS = \
    test.UsuarioAdminTest \
    test.UsuarioRespondedorTest

ALL_TESTS = $(ALG_TESTS) $(PREG_TESTS) $(OTHER_TESTS)

# ==============================
# TARGETS PRINCIPALES
# ==============================
.PHONY: all test alg-tests preg-tests run-one exes javadoc open-javadoc clean help

# Por defecto: compilar todo
all: compile-test compile-drivers jar-app javadoc help

# Crear directorio EXE si no existe
$(EXE):
	mkdir -p $(EXE)

# Compilar código principal (dominio + persistence + presentation)
compile-main: $(EXE)
	@echo "Compilando codigo principal..."
	$(JAVAC) --release 21 \
		-cp "$(CLASSPATH_COMPILE)" \
		-sourcepath $(SRC_ROOT_MAIN) \
		-d $(EXE) \
		$(SRC_MAIN)
	@echo "Compilacion principal completada."

# Compilar tests
compile-test: compile-main
	@echo "Compilando tests..."
	@if [ -d "$(SRC_ROOT_TEST)" ] && [ -n "$(SRC_TEST)" ]; then \
		$(JAVAC) --release 21 \
			-cp "$(CLASSPATH_TESTS)" \
			-sourcepath "$(SRC_ROOT_TEST)" \
			-d $(EXE) \
			$(SRC_TEST); \
	fi
	@echo "Compilacion de tests completada."

# Compilar drivers (si existen)
compile-drivers: compile-main
	@echo "Compilando drivers..."
	@if [ -d "$(SRC_ROOT_DRIVERS)" ] && [ -n "$(SRC_DRIVERS)" ]; then \
		$(JAVAC) --release 21 \
			-cp "$(CLASSPATH_MAIN)" \
			-sourcepath "$(SRC_ROOT_DRIVERS)" \
			-d $(EXE) \
			$(SRC_DRIVERS); \
	fi
	@echo "Compilacion de drivers completada."

# ==============================
# EJECUCIÓN DE TESTS
# ==============================

# Ejecutar TODOS los tests
test: compile-test
	@echo "Ejecutando todos los tests..."
	$(JAVA) -cp "$(CLASSPATH_TESTS)" org.junit.runner.JUnitCore $(ALL_TESTS)

# Ejecutar SOLO tests de algoritmos
alg-tests: compile-test
	@echo "Ejecutando tests de algoritmos..."
	$(JAVA) -cp "$(CLASSPATH_TESTS)" org.junit.runner.JUnitCore $(ALG_TESTS)

# Ejecutar SOLO tests de preguntas/encuestas
preg-tests: compile-test
	@echo "Ejecutando tests de preguntas..."
	$(JAVA) -cp "$(CLASSPATH_TESTS)" org.junit.runner.JUnitCore $(PREG_TESTS)

# Ejecutar SOLO UN test concreto
# Uso: make run-one TEST=TestKMeans
run-one: compile-test
	@echo "Ejecutando test $(TEST)..."
	$(JAVA) -cp "$(CLASSPATH_TESTS)" org.junit.runner.JUnitCore $(TEST)

# ==============================
# CREAR JARS EJECUTABLES DE DRIVERS
# ==============================

# JAR para DriverCtrlEncuestas
jar-encuestas: compile-drivers
	@echo "Main-Class: drivers.DriverCtrlEncuestas" > $(EXE)/MANIFEST.MF
	@echo "Class-Path: ../lib/gson-2.10.1.jar" >> $(EXE)/MANIFEST.MF
	jar cfm $(EXE)/driver-encuestas.jar $(EXE)/MANIFEST.MF -C $(EXE) .
	@echo "JAR driver encuestas creado: $(EXE)/driver-encuestas.jar"

# JAR para DriverCtrlRespuestas
jar-respuestas: compile-drivers
	@echo "Main-Class: drivers.DriverCtrlRespuestas" > $(EXE)/MANIFEST.MF
	@echo "Class-Path: ../lib/gson-2.10.1.jar" >> $(EXE)/MANIFEST.MF
	jar cfm $(EXE)/driver-respuestas.jar $(EXE)/MANIFEST.MF -C $(EXE) .
	@echo "JAR driver respuestas creado: $(EXE)/driver-respuestas.jar"

# JAR para DriverCtrlClustering
jar-clustering: compile-drivers
	@echo "Main-Class: drivers.DriverCtrlClustering" > $(EXE)/MANIFEST.MF
	@echo "Class-Path: ../lib/gson-2.10.1.jar" >> $(EXE)/MANIFEST.MF
	jar cfm $(EXE)/driver-clustering.jar $(EXE)/MANIFEST.MF -C $(EXE) .
	@echo "JAR driver clustering creado: $(EXE)/driver-clustering.jar"

# Crear todos los JARs de drivers
jar-drivers: jar-encuestas jar-respuestas jar-clustering
	@echo "Todos los JARs de drivers creados."

# ==============================
# EJECUTAR DRIVERS
# ==============================

# Ejecutar driver de encuestas
run-encuestas: jar-encuestas
	@echo "Ejecutando Driver de Encuestas..."
	$(JAVA) -jar $(EXE)/driver-encuestas.jar

# Ejecutar driver de respuestas
run-respuestas: jar-respuestas
	@echo "Ejecutando Driver de Respuestas..."
	$(JAVA) -jar $(EXE)/driver-respuestas.jar

# Ejecutar driver de clustering
run-clustering: jar-clustering
	@echo "Ejecutando Driver de Clustering..."
	$(JAVA) -jar $(EXE)/driver-clustering.jar

# ==============================
# JAR EJECUTABLE PRINCIPAL
# ==============================

jar-app: compile-main
	@echo "Creando JAR ejecutable principal..."
	@echo "Main-Class: main.presentation.Main" > $(EXE)/MANIFEST.MF
	@echo "Class-Path: ../lib/gson-2.10.1.jar ../lib/javafx-sdk-21.0.9/lib/javafx.controls.jar ../lib/javafx-sdk-21.0.9/lib/javafx.fxml.jar ../lib/javafx-sdk-21.0.9/lib/javafx.graphics.jar ../lib/javafx-sdk-21.0.9/lib/javafx.base.jar" >> $(EXE)/MANIFEST.MF
	jar cfm $(EXE)/aplicacion.jar $(EXE)/MANIFEST.MF -C $(EXE) .
	@echo "JAR principal creado: $(EXE)/aplicacion.jar"

run-app: jar-app
	$(JAVA) --module-path "$(JAVAFX_LIB)" \
		--add-modules $(JAVAFX_MODULES) \
		-jar $(EXE)/aplicacion.jar


# ==============================
# GENERACIÓN DE DOCUMENTACIÓN
# ==============================

# Carpeta donde se generará el Javadoc
JAVADOC_DIR = DOCS/Javadoc

# Generar Javadoc (usa todos los archivos .java encontrados)
javadoc: compile-main
	@echo "Generando Javadoc..."
	@mkdir -p $(JAVADOC_DIR)
	@echo "Documentando: domain, persistence, presentation"
	$(JAVADOC) -d $(JAVADOC_DIR) \
		-sourcepath $(SRC_ROOT_MAIN) \
		-classpath "$(CLASSPATH_COMPILE)" \
		-encoding UTF-8 \
		-charset UTF-8 \
		-docencoding UTF-8 \
		-author \
		-version \
		-use \
		-private \
		$(SRC_MAIN)
	@echo "✓ Javadoc generado en: $(JAVADOC_DIR)/index.html"

# Generar Javadoc solo para paquetes públicos (sin -private)
javadoc-public: compile-main
	@echo "Generando Javadoc (solo APIs publicas)..."
	@mkdir -p $(JAVADOC_DIR)
	$(JAVADOC) -d $(JAVADOC_DIR) \
		-sourcepath $(SRC_ROOT_MAIN) \
		-classpath "$(CLASSPATH_COMPILE)" \
		-encoding UTF-8 \
		-charset UTF-8 \
		-docencoding UTF-8 \
		-author \
		-version \
		-use \
		$(SRC_MAIN)
	@echo "✓ Javadoc publico generado en: $(JAVADOC_DIR)/index.html"

# Abrir Javadoc en el navegador
open-javadoc: javadoc
	@if [ -f "$(JAVADOC_DIR)/index.html" ]; then \
		echo "Abriendo Javadoc en el navegador..."; \
		if command -v xdg-open > /dev/null 2>&1; then \
			xdg-open $(JAVADOC_DIR)/index.html; \
		elif command -v open > /dev/null 2>&1; then \
			open $(JAVADOC_DIR)/index.html; \
		elif command -v start > /dev/null 2>&1; then \
			start $(JAVADOC_DIR)/index.html; \
		else \
			echo "No se pudo abrir automaticamente. Abre manualmente: $(JAVADOC_DIR)/index.html"; \
		fi \
	fi

# ==============================
# LIMPIEZA
# ==============================

init-data:
	@echo "Restaurando datos base en $(DATA_DIR)..."
	@mkdir -p $(DATA_DIR)
	@cp -a $(SEED_DIR)/* $(DATA_DIR)/ 2>/dev/null || true
	@echo "✓ Datos base restaurados."

clean-data:
	@echo "Limpiando datos de persistencia..."
	@rm -rf $(DATA_DIR)/*
	@$(MAKE) init-data
	@echo "Limpieza de datos completada (datos base restaurados)."

clean-data-hard:
	@echo "Limpiando datos de persistencia..."
	@rm -rf $(DATA_DIR)/*
	@echo "Limpieza de datos completada (sa han eliminado todos los datos)."	

clean:
	@echo "Limpiando archivos compilados y datos runtime..."
	@rm -rf $(EXE)/drivers
	@rm -rf $(EXE)/main
	@rm -rf $(EXE)/test
	@rm -rf $(EXE)/*.jar	
	@rm -rf $(EXE)/*.MF	
	@rm -rf $(DATA_DIR)/*
	@$(MAKE) clean-javadoc
	@$(MAKE) init-data
	@echo "Limpieza completada (seeds restaurados)."

clean-javadoc:
	@echo "Limpiando documentacion Javadoc..."
	rm -rf $(JAVADOC_DIR)
	@echo "Javadoc eliminado."

# ==============================
# AYUDA
# ==============================

help:
	@echo "╔════════════════════════════════════════════════════════╗"
	@echo "║            MAKEFILE - SISTEMA DE ENCUESTAS             ║"
	@echo "╚════════════════════════════════════════════════════════╝"
	@echo ""
	@echo "COMPILACIÓN:"
	@echo "  make all              - Compilar todo (main + test + drivers)"
	@echo "  make compile-main     - Compilar solo codigo principal"
	@echo "  make compile-test     - Compilar tests"
	@echo "  make compile-drivers  - Compilar drivers"
	@echo ""
	@echo "TESTS:"
	@echo "  make test             - Ejecutar todos los tests"
	@echo "  make alg-tests        - Ejecutar tests de algoritmos"
	@echo "  make preg-tests       - Ejecutar tests de preguntas"
	@echo "  make run-one TEST=X   - Ejecutar un test especifico"
	@echo "    Ejemplo: make run-one TEST=TestKMeans"
	@echo ""
	@echo "DRIVERS (Tests Interactivos):"
	@echo "  make jar-drivers      - Crear todos los JARs de drivers"
	@echo "  make jar-encuestas    - Crear JAR de driver de encuestas"
	@echo "  make jar-respuestas   - Crear JAR de driver de respuestas"
	@echo "  make jar-clustering   - Crear JAR de driver de clustering"
	@echo ""
	@echo "  make run-encuestas    - Ejecutar driver de encuestas"
	@echo "  make run-respuestas   - Ejecutar driver de respuestas"
	@echo "  make run-clustering   - Ejecutar driver de clustering"
	@echo ""
	@echo "APLICACIÓN PRINCIPAL:"
	@echo "  make jar-app          - Crear JAR de la aplicacion JavaFX"
	@echo "  make run-app          - Ejecutar aplicacion JavaFX"
	@echo ""
	@echo "DOCUMENTACIÓN:"
	@echo "  make javadoc          - Generar documentacion completa (privado + publico)"
	@echo "  make javadoc-public   - Generar solo documentacion publica"
	@echo "  make open-javadoc     - Generar y abrir Javadoc en navegador"
	@echo "  make clean-javadoc    - Eliminar documentacion generada"
	@echo ""
	@echo "LIMPIEZA:"
	@echo "  make clean            - Limpiar .class y datos"
	@echo "  make clean-data       - Limpiar solo datos de persistencia (sin contar los iniciales)"
	@echo "  make clean-data-hard  - Limpiar solo datos de persistencia (todos)"
	@echo "  make init-data        - Cargar los juegos de prueba iniciales"
	@echo ""
	@echo "AYUDA:"
	@echo "  make help             - Mostrar esta ayuda"