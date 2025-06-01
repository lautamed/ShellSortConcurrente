package modelo;

public class ShellSortSecuencial {
    // Método que implementa ShellSort de forma secuencial
    public static void shellSort(int[] array) {
        int n = array.length;

        // Comenzamos con un "gap" grande y lo vamos reduciendo
        for (int gap = n / 2; gap > 0; gap /= 2) {
            // Aplicamos Insertion Sort para este "gap"
            for (int i = gap; i < n; i++) {
                int temp = array[i]; // Guardamos el valor actual
                int j;
                // Comparamos elementos separados por el "gap"
                for (j = i; j >= gap && array[j - gap] > temp; j -= gap) {
                    array[j] = array[j - gap]; // Desplazamos hacia adelante
                }
                array[j] = temp; // Insertamos el valor donde corresponde
            }
        }
    }
}