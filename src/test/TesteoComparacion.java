package test;

import modelo.ShellSortSecuencial;
import modelo.ShellSortConcurrente;

import java.util.Arrays;
import java.util.Random;

// Clase de testeo para comparar el rendimiento entre ShellSort secuencial y concurrente
public class TesteoComparacion {

    // Genera un arreglo aleatorio de tamaño especificado
    public static int[] generarArregloAleatorio(int tamaño) {
        int[] arreglo = new int[tamaño];
        
        // Semilla fija para resultados reproducibles entre ejecuciones
        Random rand = new Random(42); // Permite comparaciones y un debugging constante
        
        // Llenamos el array con valores aleatorios del 0 al 99.999
        for (int i = 0; i < tamaño; i++) {
            arreglo[i] = rand.nextInt(100000); 
        }
        return arreglo;
    }

    // Ejecuta y muestra los resultados para un tamaño de arreglo dado
    public static void ejecutarPrueba(int tamaño) {
        System.out.println("\n--- Tamaño del arreglo: " + tamaño + " ---");

        int[] original = generarArregloAleatorio(tamaño);// Generamos el arreglo original
        int[] arregloSecuencial = original.clone(); 	 // Copiamos el arreglo original para la versión secuencial
        int[] arregloConcurrente = original.clone();	 // Copiamos el arreglo original para la versión concurrente

        // Calentamiento de JVM
        // Se mejora la precisión en las mediciones eliminando overhead de compilación
        if (tamaño >= 100000) {
        	
        	//Ejecutamos ambos algoritmos una vez para que JIT compile el código
            int[] warmup = original.clone();
            ShellSortSecuencial.shellSort(warmup);
            warmup = original.clone();
            ShellSortConcurrente.shellSortConcurrente(warmup);
        }

        // Versión secuencial, usamos nanoTime para mayor precisión
        long inicioSecuencial = System.nanoTime();
        ShellSortSecuencial.shellSort(arregloSecuencial);
        long finSecuencial = System.nanoTime();
        
        //Convertimos nanosegundos a milisegundos(ms) para una mejor presentación
        double tiempoSecuencial = (finSecuencial - inicioSecuencial) / 1_000_000.0;
        System.out.printf("Tiempo secuencial: %.2f ms%n", tiempoSecuencial);
 
        // Versión concurrente
        long inicioConcurrente = System.nanoTime();
        ShellSortConcurrente.shellSortConcurrente(arregloConcurrente);
        long finConcurrente = System.nanoTime();
        //Convertimos nanosegundos a milisegundos(ms) para una mejor presentación
        double tiempoConcurrente = (finConcurrente - inicioConcurrente) / 1_000_000.0;
        System.out.printf("Tiempo concurrente: %.2f ms%n", tiempoConcurrente);

        // Calculo de SpeedUp
        //Speedup > 1.0 indica que la versión concurrente es más rápida 
        double speedup = tiempoSecuencial / tiempoConcurrente;
        System.out.printf("Speedup: %.2fx %s%n", speedup, 
            speedup > 1.0 ? "✅ (Mejora)" : "❌ (Empeora)");

        // Comparamos que ambos algoritmos produzcan el mismo resultado
        boolean sonIguales = Arrays.equals(arregloSecuencial, arregloConcurrente);
        System.out.println("¿Resultados iguales?: " + (sonIguales ? "Sí ✅" : "No ❌"));
        
        // Verificamos que el array este ordenado correctamente
        boolean estaOrdenado = verificarOrdenado(arregloConcurrente);
        System.out.println("¿Array ordenado?: " + (estaOrdenado ? "Sí ✅" : "No ❌"));
    }
    
    // Verifica si un array está ordenado
    private static boolean verificarOrdenado(int[] array) {
        for (int i = 1; i < array.length; i++) {
            if (array[i] < array[i-1]) {
                return false;
            }
        }
        return true;
    }

    
    public static void main(String[] args) {
        System.out.println("=== Comparación ShellSort: Secuencial vs Concurrente ===");
        System.out.println("Procesadores disponibles: " + Runtime.getRuntime().availableProcessors());
        
        // Probamos con diferentes tamaños
        int[] tamaños = {10000, 50000, 100000, 500000, 1000000, 10000000, 100000000};
        
        for (int tamaño : tamaños) {
            ejecutarPrueba(tamaño);
        }
        
        // Importante: Cerrar el pool de hilos
        ShellSortConcurrente.shutdown();
        
        System.out.println("\n=== Pruebas completadas ===");
    }
}