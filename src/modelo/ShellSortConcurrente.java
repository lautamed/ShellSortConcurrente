package modelo;

import java.util.concurrent.*;

public class ShellSortConcurrente {
    private static final int THRESHOLD = 50000; // Umbral para usar concurrencia (1era decision)
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors(); //2da decision!
    
    //3era decision
    //Usar ThreadPool en lugar de crear hilos constantemente
    private static final ExecutorService executor = 
        Executors.newFixedThreadPool(MAX_THREADS);
    
    static class ShellSortTask implements Callable<Void> {
        private final int[] array;
        private final int gap;
        private final int startIndex;
        private final int endIndex;
        
        //Constructor que define el rango de subsecuencias
        public ShellSortTask(int[] array, int gap, int startIndex, int endIndex) {
            this.array = array;
            this.gap = gap;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
        
        @Override
        public Void call() {
            // Procesar múltiples subsecuencias por hilo para reducir overhead
            for (int start = startIndex; start < Math.min(gap, endIndex); start++) {
                for (int i = start + gap; i < array.length; i += gap) {
                    int temp = array[i];
                    int j = i;
                    
                    
                    while (j >= gap && array[j - gap] > temp) { //Insertamos el array [i] en la posicion correcta dentro de su subsecuencia
                        array[j] = array[j - gap];
                        j -= gap;
                    }
                    array[j] = temp;
                }
            }
            return null;
        }
    }
    
    public static void shellSortConcurrente(int[] array) {
        int n = array.length;
        
        //Primera decision clave
        // Para arrays pequeños, usar versión secuencial
        
        if (n < THRESHOLD) {
            shellSortSecuencial(array);
            return;
        }
        
        for (int gap = n / 2; gap > 0; gap /= 2) {
            // Para gaps pequeños, usar versión secuencial para evitar overhead
            if (gap < MAX_THREADS || gap < 4) {
                shellSortSecuencialConGap(array, gap);
                continue;
            }
            
            // Calcular número óptimo de hilos segun el gap actual y la cantidad maxima disponible
            int numThreads = Math.min(MAX_THREADS, gap);
            // Calculamos cuantas subsecuencias manejara cada hilo
            int subsecuenciasPorHilo = (gap + numThreads - 1) / numThreads; //4ta decision
            
            Future<?>[] futures = new Future<?>[numThreads];
            
            try {
                // Distribuir trabajo entre hilos
                for (int i = 0; i < numThreads; i++) {
                    int startIndex = i * subsecuenciasPorHilo;
                    int endIndex = Math.min((i + 1) * subsecuenciasPorHilo, gap);
                    
                    if (startIndex < gap) {
                        futures[i] = executor.submit(
                            new ShellSortTask(array, gap, startIndex, endIndex)
                        );
                    }
                }
                
                // Esperar a que terminen todos los hilos
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
    
    // Método para cerrar el pool de hilos (llamar al final del programa)
    public static void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}