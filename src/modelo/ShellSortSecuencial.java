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














//   0   1   2   3   4   5   6   7
// [64, 34, 25, 12, 22, 11, 35, 14] gap=4, comparamos posicion 0 y 4 (64 y 22)
//   
// [22, 34, 25, 12, 64, 11, 35, 14]
//----------------------------------------
// [64, 34, 25, 12, 22, 11, 35, 14] gap=4, comparamos posicion 1 y 5 (34 y 11)
//
// [22, 11, 25, 12, 64, 34, 35, 14]
//----------------------------------------
// [64, 34, 25, 12, 22, 11, 35, 14] gap=4, comparamos posicion 2 y 6 (25 y 35)
//
// [22, 11, 25, 12, 64, 34, 35, 14]
//----------------------------------------
// [64, 34, 25, 12, 22, 11, 35, 14] gap=4, comparamos posicion 3 y 7 (12 y 14)
//
// [22, 11, 25, 12, 64, 34, 35, 14]
//---------------------------------------