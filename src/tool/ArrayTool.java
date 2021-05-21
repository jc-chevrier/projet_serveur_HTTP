package tool;

import java.util.Arrays;

/**
 * Classe proposant des outils sur les tableaux.
 *
 * @author CHEVRIER Jean-Christophe, HADJ MESSAOUD Yousra, LOUGADI Marième,
 *         étudiants en MASTER 1 MIAGE, à l'université de Lorraine.
 */
public class ArrayTool {
    /**
     * Concaténer des tableaux d'octets.
     *
     * @param array1
     * @param array2
     * @return
     */
    public static byte[] concatenateBytesArrays(byte[] array1, byte[] array2) {
        int lengthArray1 = array1.length;
        int lengthArray2 = array2.length;
        byte[] mergeArrays = Arrays.copyOf(array1, lengthArray1 + lengthArray2);
        System.arraycopy(array2, 0, mergeArrays, lengthArray1, lengthArray2);
        return mergeArrays;
    }
}
