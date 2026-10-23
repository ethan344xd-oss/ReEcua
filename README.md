# Calculadora de ecuaciones

Programa de escritorio en Java que resuelve sistemas de ecuaciones **paso a paso** con cuatro métodos: sustitución, igualación, reducción y gráfico.

## Características

- **Cuatro métodos en pestañas separadas:** sustitución, igualación, reducción y gráfico, cada uno con su propio paso a paso.
- **Navegación por pasos:** botones Inicio, Anterior, Siguiente y Todo, con el paso actual resaltado.
- **Método gráfico animado:** despeja, arma la tabla de valores, dibuja cada ecuación una por una y marca el punto de corte. Se puede hacer zoom con la rueda y mover con el mouse.
- **Fracciones exactas:** nunca redondea a mitad de camino.
- **Escritura flexible:** acepta `x`, `X`, `×`, `2(x+1)`, `x/2`, `0,5`, `xy`, `x^2` y `x²`.
- **Sistemas no lineales** (con el método de sustitución): productos, potencias, ecuaciones de segundo grado y raíces irracionales.
- **Elegir el despeje:** puedes escoger qué incógnita despejar y en qué ecuación.
- **Modo práctica:** genera sistemas al azar y comprueba tus respuestas.
- Verificación automática, modo oscuro, y opciones para guardar o copiar la resolución.

## Requisitos

- Java 8 o superior (se recomienda un JDK 21, por ejemplo Temurin de [adoptium.net](https://adoptium.net)).

## Cómo ejecutarlo

Descarga `CalculadoraEcuaciones.jar` desde la sección [Releases](https://github.com/ethan344xd-oss/ReEcua/releases) y ábrelo con doble clic, o desde la terminal:

```
java -jar CalculadoraEcuaciones.jar
```

## Cómo usarlo

1. Escribe las ecuaciones en el cuadro de la izquierda, una por línea.
2. Pulsa **Resolver** (o Ctrl + Enter).
3. Elige la pestaña del método que quieras ver.
4. Usa **Siguiente** y **Anterior** para avanzar de a un paso.

Ejemplos:

```
2x + y = 5
x - y = 1
```

```
x^2 + y^2 = 25
x - y = 1
```

## Uso en clase

Se puede usar como una ayuda para que los alumnos no solo vayan y pidan a un sitio, si no que puedan ver y entender en menos tiempo (por ejemplo: cómo usar el modo práctica, el paso a paso para explicar un método, etc.)

## Compilar desde el código

```
javac -encoding UTF-8 -d out src/Main.java src/Motor.java src/Grafica.java
java -cp out Main
```

Para generar el `.jar` (si usas `logo.png`, cópialo antes dentro de la carpeta `out`):

```
jar cfe CalculadoraEcuaciones.jar Main -C out .
```

## Estructura del proyecto

- `Main.java`: ventana, menús, pestañas y modo práctica.
- `Motor.java`: lectura de ecuaciones, álgebra exacta y los métodos de resolución.
- `Grafica.java`: panel que dibuja las ecuaciones y los puntos de corte.

## Limitaciones

- Igualación y reducción necesitan poder despejar o eliminar una incógnita en todas las ecuaciones donde aparece. Para sistemas no lineales usa sustitución.
- El método gráfico solo funciona con 2 incógnitas.
- Los exponentes van de 1 a 9, la división solo es entre números, y solo se buscan soluciones reales.

## Licencia

Distribuido bajo la licencia MIT. Consulta el archivo

## Autor

Uc Lobato Ethan Rafael · [@ethan344xd-oss](https://github.com/ethan344xd-oss)
