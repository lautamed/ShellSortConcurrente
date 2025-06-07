package modelo;

import java.util.concurrent.*;

//Implementación concurrente del algoritmo ShellSort

public class ShellSortConcurrente {
	//1era DECISIÓN: Umbral para determinar cuando usar concurrencia
    private static final int THRESHOLD = 50000; // Umbral, arrays menores a este tamaño se procesan secuencialmente para evitar overhead
    
    
    //2da DECISIÓN: Número máximo de hilos, según la cantidad de procesadores disponibles.
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();
    //Evitamos manejar más hilos de los que el sistema puede manejar de manera eficiente
    
    
    //3era DECISIÓN: Pool de hilos REUTILIZABLES
    //Usamos ThreadPool en lugar de crear hilos constantemente, para reducir el overhead y mejorar la gestión de recursos
    private static final ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
    
    
    //Clase que representa una tarea para procesar subsecuencias del algoritmo ShellSort
    static class ShellSortTask implements Callable<Void> { //Implementa "Callable" para el retorno de valores y manejar exceptions
        private final int[] array;
        private final int gap;        // Distancia entre elementos
        private final int startIndex; // Index inicial de subsecuencias a procesar
        private final int endIndex;   // Index final de subsecuencias a procesar
        
        //Constructor que define el rango de subsecuencias que procesa este hilo
        public ShellSortTask(int[] array, int gap, int startIndex, int endIndex) {
            this.array = array;
            this.gap = gap;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
        
        @Override
        public Void call() {
            // Procesar múltiples subsecuencias por hilo para reducir overhead de sincronización
        	// Cada hilo maneja un rango de subsecuencias contiguas
            for (int start = startIndex; start < Math.min(gap, endIndex); start++) {
            	
            	// Aplicamos Insertion Sort en la subsecuencia que arranca con "start"
                for (int i = start + gap; i < array.length; i += gap) {
                    int temp = array[i]; // Elementos a insertar en la posición
                    int j = i;           // Posición actual para comparar
                    
                    // Buscamos hacia atras en la subsecuencia para encontrar la posición correcta
                    while (j >= gap && array[j - gap] > temp) { //Insertamos el array [i] en la posicion correcta dentro de su subsecuencia
                        array[j] = array[j - gap];  // Desplazamos el elemento hacia adelante
                        j -= gap;					// Moverse al siguiente elemento de la subsecuencia
                    }
                    array[j] = temp; // Elemento en la posición final
                }
            }
            return null; 
        }
    }
    
    public static void shellSortConcurrente(int[] array) {
        int n = array.length;
        
        //1er DECISIÓN clave: para arrays pequeños, usamos la versión secuencial
        
        if (n < THRESHOLD) {
            shellSortSecuencial(array);
            return;
        }
        
        //Iteración principal del ShellSort, reducimos el gap de forma progresiva
        for (int gap = n / 2; gap > 0; gap /= 2) {
        	
            // Para gaps pequeños, usar versión secuencial
        	
            if (gap < MAX_THREADS || gap < 4) { //Cuando gap < MAX_THREADS, no hay suficientes subsecuencias para hacer en paralelo.
                shellSortSecuencialConGap(array, gap);
                continue; // pasar al siguiente gap sin usar concurrencia
            }
            
            //4ta Decisión clave: distribución eficiente de trabajo
            
            // Calcular número óptimo de hilos segun el gap actual y la cantidad maxima disponible
            int numThreads = Math.min(MAX_THREADS, gap);
            
            // Calculamos cuantas subsecuencias manejara cada hilo
            int subsecuenciasPorHilo = (gap + numThreads - 1) / numThreads; //4ta decision
            
            
            //Array para almacenar referencias a las tareas asíncronas
            Future<?>[] futures = new Future<?>[numThreads];
            
            try {
                // Distribuir trabajo entre hilos
                for (int i = 0; i < numThreads; i++) {
                	
                	//Calculas rango de subsecuencias para este hilo
                    int startIndex = i * subsecuenciasPorHilo;
                    int endIndex = Math.min((i + 1) * subsecuenciasPorHilo, gap);
                    
                    // Solo creamos tarea si hay subsecuencias para procesar
                    if (startIndex < gap) {
                        futures[i] = executor.submit(
                            new ShellSortTask(array, gap, startIndex, endIndex)
                        );
                    }
                }
                
                // SINCRONIZACIÓN: Esperamos a que terminen todos los hilos
                // Es crucial que todos terminen antes de pasar al siguiente gap
                for (Future<?> future : futures) {
                    if (future != null) {
                        future.get();
                    }
                }
                
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                // En caso de error, usar versión secuencial
                shellSortSecuencialConGap(array, gap);
            }
        }
    }
    
    // Versión secuencial para arrays pequeños
    // Evitamos overhead
    private static void shellSortSecuencial(int[] array) {
        int n = array.length;
        for (int gap = n / 2; gap > 0; gap /= 2) {
            shellSortSecuencialConGap(array, gap);
        }
    }
    
    // Aplicar insertion sort para un gap específico (versión secuencial)
    private static void shellSortSecuencialConGap(int[] array, int gap) {
        for (int i = gap; i < array.length; i++) {
            int temp = array[i];
            int j;
            for (j = i; j >= gap && array[j - gap] > temp; j -= gap) {
                array[j] = array[j - gap];
            }
            array[j] = temp;
        }
    }
    
    // Método para cerrar el pool de hilos
    // Se llama al final del programa para liberar recursos
    public static void shutdown() {
        executor.shutdown(); // No acepta tareas nuevas
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) { // Esperamos 1 minuto para terminar tareas pendientes
                executor.shutdownNow(); // Forzamos de ser necesario
            }
        } catch (InterruptedException e) {
            executor.shutdownNow(); //Forzamos si es interrumpido
        }
    }
}