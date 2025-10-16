# Experimento: False Sharing en Java

Este repositorio contiene un pequeño experimento práctico para observar un fenómeno conocido como False Sharing en la JVM (Java Virtual Machine).

El objetivo es mostrar cómo el acceso concurrente a variables cercanas en memoria —incluso si pertenecen a distintas clases— puede afectar significativamente el rendimiento cuando dos o más hilos escriben sobre datos que residen en la misma línea de caché.

## ¿Qué es el False Sharing?

En procesadores modernos (por ejemplo, de 64 bits), la memoria caché está organizada en bloques llamados líneas de caché, generalmente de 64 bytes.

Cuando un hilo accede a una variable, el procesador carga en su caché toda la línea de memoria que contiene ese dato.
El problema aparece cuando otro hilo necesita acceder (y especialmente escribir) sobre otra variable que cae dentro de la misma línea de caché. En ese momento entra en juego el protocolo de coherencia de caché:

Cada hilo mantiene su propia copia de la línea.

Si un hilo modifica un valor, esa copia se marca como "modificada".

Las copias de los demás hilos se invalidan, obligándolos a recargar la línea desde la memoria compartida.

Este intercambio constante de invalidaciones y recargas genera una degradación notable del rendimiento, especialmente en operaciones intensivas de lectura/escritura concurrente.

``⚠️ No se trata de un fallo de la JVM, sino de una consecuencia natural del diseño de los procesadores modernos, optimizado para minimizar accesos a memoria principal.``

## Cómo evitar el False Sharing

Existen varias formas de evitar este problema:

1. Añadir padding manual entre variables para asegurar que cada una ocupe su propia línea de caché.

2. Usar la anotación ``@Contended`` junto con el flag ``-XX:-RestrictContended``, que hace que la JVM inserte automáticamente el espacio de separación necesario.

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