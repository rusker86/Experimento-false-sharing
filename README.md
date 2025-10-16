# Análisis Experimental del False Sharing en Java y su impacto en la JVM


El presente experimento forma parte de una serie de pruebas orientadas a comprender cómo los mecanismos de coherencia de caché afectan el rendimiento de aplicaciones concurrentes en Java
------
Este repositorio contiene un pequeño experimento práctico para observar un fenómeno conocido como False Sharing en la JVM (Java Virtual Machine).

El objetivo es mostrar cómo el acceso concurrente a variables cercanas en memoria —incluso si pertenecen a distintas clases— puede afectar significativamente el rendimiento cuando dos o más hilos escriben sobre datos que residen en la misma línea de caché.

## ¿Qué es el False Sharing?

En procesadores modernos (por ejemplo, de 64 bits), la memoria caché está organizada en bloques llamados líneas de caché, generalmente de 64 bytes.

Cuando un hilo accede a una variable, el procesador carga en su caché toda la línea de memoria que contiene ese dato.
El problema aparece cuando otro hilo necesita acceder (y especialmente escribir) sobre otra variable que cae dentro de la misma línea de caché. En ese momento entra en juego el **protocolo de coherencia de caché**:

- Cada hilo mantiene su propia copia de la línea.

- Si un hilo modifica un valor, esa copia se marca como "modificada".

- Las copias de los demás hilos se invalidan, obligándolos a recargar la línea desde la memoria compartida.

Este intercambio constante de invalidaciones y recargas genera una degradación notable del rendimiento, especialmente en operaciones intensivas de lectura/escritura concurrente.

``⚠️ No se trata de un fallo de la JVM, sino de una consecuencia natural del diseño de los procesadores modernos, optimizado para minimizar accesos a memoria principal.``

## Cómo evitar el False Sharing

Existen varias formas de evitar este problema:

1. Añadir padding manual entre variables para asegurar que cada una ocupe su propia línea de caché.

2. Usar la anotación ``@Contended`` junto con el flag ``-XX:-RestrictContended``, que hace que la JVM inserte automáticamente el espacio de separación necesario (Introducido en Java 8).

## Código del experimento

El proyecto define dos clases:

``CounterWithFalseSharing``

Contiene dos contadores (counter1 y counter2) ubicados contiguamente en memoria.
El acceso concurrente provoca false sharing, degradando el rendimiento.

``CounterWithoutFalseSharing``

Introduce un padding manual (57 bytes adicionales más los 8 bytes del long) para alcanzar los 64 bytes de una línea de caché, garantizando que cada contador resida en una línea diferente.

## Ejecución del experimento

Se lanzan varios hilos que incrementan los contadores mil millones de veces.
El tiempo de ejecución de cada hilo se mide para comparar el impacto del false sharing frente a la versión con padding.

### En la siguiente tabla se ven los efectos del false sharing cuando trabajan dos hilos: Dos con false sharing y otros dos sin false sharing

|Nº Hilos  | Tiempos  |
|----------|----------|
| Hilo 1 (count 1 - Con false sharing)| 20.50 segundos|
| Hilo 2 (count 2 - Con false sharing)| 20.59 segundos|
| Hilo 4 (count 1 - Sin false sharing)| 8.78 segundos |
| Hilo 5 (count 2 - Sin false sharing)| 8.76 segundos |

-----------
En pruebas anteriores sin documentar los tiempos de los hilos 1 y 2 daban resultados de ~30 segundos. Se sospecha que puede ser el JIT (Just In Time) aplicando optimizaciones agresivas. Para confirmarlo se hizo una nueva prueba desactivando el JIT con el flag `-Xint` (Por motivos de rendimiento solo debe usarse en entornos de observación) Se obtenieron los siguientes resultados

|Nº Hilos  | Tiempos (Con JIT desactivado)  |
|----------|----------|
| Hilo 1 (count 1 - Con false sharing)| 49.13 segundos|
| Hilo 2 (count 2 - Con false sharing)| 48.40 segundos|
| Hilo 4 (count 1 - Sin false sharing)| 10.09 segundos|
| Hilo 5 (count 2 - Sin false sharing)| 10.10 segundos|
-------------
### En la siguiente tabla se ven los efectos del false sharing cuando trabajan tres hilos: tres con false sharing y otros tres sin false sharing

|Nº Hilos  | Tiempos  |
|----------|----------|
| Hilo 1 (count 1 - Con false sharing)    | 67.33 segundos   |
| Hilo 2 (count 2 - Con false sharing)    | 66.66 segundos   |
| Hilo 3 (count 2 - Con false sharing)    | 67.009 segundos  |
| Hilo 4 (count 1 - Sin false sharing)    | 5.80 segundos    |
| Hilo 5 (count 1 - Sin false sharing)    | 18.86 segundos   |
| Hilo 6 (count 2 - Sin false sharing)    | 18.79 segundos   |

#### Conclusión:
Los resultados confirman que el false sharing no es un fallo de la JVM, sino un efecto inherente a la arquitectura de caché de los procesadores modernos.
La JVM, mediante el JIT, puede mitigar parcialmente su impacto al generar código optimizado adaptado al perfil de ejecución, pero no puede eliminarlo completamente.
Por otro lado, la separación física de datos (padding o @Contended) demuestra ser una solución efectiva y medible, reduciendo el tiempo de ejecución hasta un 85 % en algunos escenarios.
Este tipo de experimentos evidencian la importancia de comprender los fundamentos de hardware incluso en lenguajes de alto nivel.

---

### Actualización
Por motivos didacticos se ha añadido una herramienta que automatiza los test de rendimiento y genera un gráfico comparativo. Si bien es inestable no afecta negativamente al rendimiento. Hasta que no se publique una versión estable se recomienda ejecutar el experimento de forma manual.

## Como ejecutar el proyecto manualmente

- Compilación: 
```shelld
javac App.java
```

- Ejecución normal (con JIT): 
```shell
java App
```

- Ejecución sin JIT:
``` shell
java -Xint App
```

- Ejecución compilada (Sin interpretación):
``` shell
java -Xcomp App
```

## Como ejecutar el proyecto con la herramienta automática
``` shell
python3 gui.py
```

# Para ejecutar las pruebas automatizadas es necesario contar con una versión 3 de python y las librerías pandas y matplotlib
