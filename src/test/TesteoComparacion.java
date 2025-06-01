package test;

import modelo.ShellSortSecuencial;
import modelo.ShellSortConcurrente;

import java.util.Arrays;
import java.util.Random;

public class TesteoComparacion {

    // Genera un arreglo aleatorio de tamaño especificado
    public static int[] generarArregloAleatorio(int tamaño) {
        int[] arreglo = new int[tamaño];
        Random rand = new Random(42); // Semilla fija para resultados reproducibles
        for (int i = 0; i < tamaño; i++) {
            arreglo[i] = rand.nextInt(100000);
        }
        return arreglo;
    }

    // Ejecuta y muestra los resultados para un tamaño de arreglo dado
    public static void ejecutarPrueba(int tamaño) {
        System.out.println("\n--- Tamaño del arreglo: " + tamaño + " ---");

        int[] original = generarArregloAleatorio(tamaño);
        int[] arregloSecuencial = original.clone();
        int[] arregloConcurrente = original.clone();

        // Calentamiento de JVM para medición más precisa
        if (tamaño >= 100000) {
            int[] warmup = original.clone();
            ShellSortSecuencial.shellSort(warmup);
            warmup = original.clone();
            ShellSortConcurrente.shellSortConcurrente(warmup);
        }

        // Versión secuencial - usar nanoTime para mayor precisión
        long inicioSecuencial = System.nanoTime();
        ShellSortSecuencial.shellSort(arregloSecuencial);
        long finSecuencial = System.nanoTime();
        double tiempoSecuencial = (finSecuencial - inicioSecuencial) / 1_000_000.0;
        System.out.printf("Tiempo secuencial: %.2f ms%n", tiempoSecuencial);

        // Versión concurrente
        long inicioConcurrente = System.nanoTime();
        ShellSortConcurrente.shellSortConcurrente(arregloConcurrente);
        long finConcurrente = System.nanoTime();
        double tiempoConcurrente = (finConcurrente - inicioConcurrente) / 1_000_000.0;
        System.out.printf("Tiempo concurrente: %.2f ms%n", tiempoConcurrente);

        // Calcular speedup
        double speedup = tiempoSecuencial / tiempoConcurrente;
        System.out.printf("Speedup: %.2fx %s%n", speedup, 
            speedup > 1.0 ? "✅ (Mejora)" : "❌ (Empeora)");

        // Comparar resultados
        boolean sonIguales = Arrays.equals(arregloSecuencial, arregloConcurrente);
        System.out.println("¿Resultados iguales?: " + (sonIguales ? "Sí ✅" : "No ❌"));
        
        // Verificar que está ordenado
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
        
        // Probar con diferentes tamaños
        int[] tamaños = {10000, 50000, 100000, 500000, 1000000, 10000000, 100000000};
        
        for (int tamaño : tamaños) {
            ejecutarPrueba(tamaño);
        }
        
        // Importante: Cerrar el pool de hilos
        ShellSortConcurrente.shutdown();
        
        System.out.println("\n=== Pruebas completadas ===");
    }
}