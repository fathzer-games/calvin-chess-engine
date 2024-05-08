package com.kelseyde.calvin.tuning.texel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kelseyde.calvin.engine.EngineConfig;
import com.kelseyde.calvin.engine.EngineInitializer;
import com.kelseyde.calvin.evaluation.Evaluator;
import com.kelseyde.calvin.utils.TestUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@Disabled
public class TexelTunerTest {

    private final TexelTuner tuner = new TexelTuner("quiet_positions.txt");
//
//    @Test
//    public void testTuneK() throws IOException {
//        tuner.tuneScalingConstant((Evaluator) TestUtils.EVALUATOR);
//    }

    @Test
    public void tunePieceValuesAndPSTs() throws IOException {

        int[] initialParams = new int[] {
                0, 0, 0, 0, 0, 0, 0, 0, 91, 127, 54, 88, 61, 119, 27, -18, -13, 0, 19, 24, 58, 63, 18, -27, -13, 10, 5, 14, 24, 19, 24, -20, -34, -9, -5, 9, 10, 13, 3, -32, -29, -11, -3, -9, 10, -4, 33, -12, -32, -1, -21, -16, -8, 31, 38, -15, 0, 0, 0, 0, 0, 0, 0, 0, -174, -89, -41, -42, 61, -96, -22, -100, -70, -34, 79, 29, 30, 69, 14, -10, -40, 53, 37, 72, 91, 136, 80, 44, -2, 24, 19, 60, 40, 69, 15, 29, -6, 11, 23, 20, 35, 26, 28, -9, -21, -6, 15, 17, 26, 17, 32, -15, -22, -46, -5, 4, 6, 20, -7, -12, -98, -19, -51, -26, -24, -21, -20, -30, -22, -3, -75, -44, -32, -35, 6, -1, -26, 9, -21, -13, 23, 66, 11, -40, -23, 30, 50, 33, 42, 50, 44, 5, -11, -2, 12, 43, 32, 30, 0, -9, -9, 6, 6, 23, 37, 5, 7, -3, 7, 22, 8, 8, 11, 20, 15, 10, -3, 22, 9, -7, 0, 21, 40, 8, -26, -10, -7, -21, -20, -15, -36, -18, 39, 49, 35, 51, 61, 16, 38, 50, 25, 25, 51, 62, 73, 74, 33, 51, 2, 19, 26, 35, 24, 52, 68, 23, -24, -11, 4, 26, 17, 28, -1, -13, -31, -25, -19, -8, 2, -5, 13, -24, -45, -24, -23, -17, -4, 0, -5, -26, -37, -17, -27, -16, -6, 4, 1, -70, -12, -12, -6, 10, 9, 10, -44, -19, -21, -1, 36, 19, 61, 51, 38, 52, -17, -32, -12, -6, -23, 64, 35, 61, -20, -19, 14, 15, 36, 63, 54, 64, -34, -20, -13, -9, 6, 20, -7, 8, -16, -26, -8, -10, 5, 3, 10, -4, -21, 1, -11, -2, -2, 4, 21, -2, -28, -1, 8, 5, 11, 16, 4, 2, -8, -19, -9, 3, -18, -32, -38, -43, -63, 24, 17, -13, -49, -34, 5, 8, 29, -1, -19, -7, -7, 3, -37, -36, -16, 24, 3, -16, -20, 6, 22, -27, -24, -27, -19, -28, -37, -25, -14, -43, -52, -2, -28, -46, -45, -49, -40, -58, -19, -13, -29, -53, -49, -31, -15, -34, 8, 0, -15, -57, -36, -23, 10, 1, -22, 29, 13, -47, 9, -31, 31, 7, 0, 0, 0, 0, 0, 0, 0, 0, 171, 166, 151, 127, 140, 125, 158, 180, 87, 93, 78, 60, 49, 46, 75, 77, 32, 17, 6, -2, -9, -3, 14, 10, 16, 6, -4, -14, -6, -1, 0, 0, 4, 0, 1, 1, 3, 2, -8, -7, 16, 1, 15, 9, 20, 7, -5, -4, 0, 0, 0, 0, 0, 0, 0, 0, -56, -45, -20, -21, -38, -27, -63, -97, -24, -15, -22, -9, -6, -24, -31, -49, -24, -27, 3, 2, -8, -16, -26, -40, -10, 10, 15, 29, 17, 11, 8, -18, -11, -6, 15, 25, 23, 10, -3, -15, -20, -4, -6, 14, 9, -5, -17, -29, -35, -13, -17, -2, -2, -17, -16, -37, -29, -51, -20, -8, -25, -11, -50, -63, -7, -28, -4, -7, -14, -9, -17, -17, -15, -11, 0, -15, -10, -20, -11, -14, 1, -15, 0, -8, -3, 7, -3, 7, -3, 2, 5, 2, 7, 3, -4, -5, -13, -4, 6, 12, 0, 3, -10, -16, -12, 0, 5, 9, 13, -4, -14, -20, -14, -19, -14, 0, 5, -10, -16, -27, -16, -10, -23, -5, -6, -23, -12, -14, 20, 13, 13, 8, 13, 19, 15, 12, 14, 20, 12, 8, -10, 10, 15, 10, 14, 14, 7, 3, 11, 4, 2, 4, 11, 10, 12, 1, 2, 8, 6, 9, 10, 7, 9, 4, 2, 1, -1, -4, 3, 2, -3, 2, -7, -10, -8, -9, 1, 1, 7, 1, -6, -6, -4, 4, -2, 9, 3, -8, -8, -6, 11, -13, -2, 29, 29, 34, 30, 26, 10, 27, -10, 20, 25, 43, 65, 32, 37, -2, -27, 3, 16, 42, 54, 42, 26, 9, -4, 25, 25, 52, 64, 47, 57, 43, -15, 35, 19, 47, 38, 34, 39, 20, -23, -34, 22, 6, 16, 14, 17, 5, -15, -16, -33, -9, -9, -30, -29, -30, -32, -21, -22, -44, -5, -37, -27, -34, -73, -42, -25, -25, -18, 8, -3, -24, -19, 10, 7, 10, 10, 38, 23, 4, 3, 10, 16, 12, 20, 38, 37, 13, -15, 15, 17, 20, 26, 31, 23, 0, -25, -11, 14, 25, 29, 30, 9, -10, -26, -6, 14, 28, 30, 23, 10, -8, -29, -4, 7, 20, 21, 11, 2, -20, -50, -34, -14, -4, -28, -12, -24, -50, 82, 344, 365, 484, 1032, 0, 87, 280, 290, 519, 936, 0
        };

        int[] bestParams = tuner.tune(initialParams, pstAndPieceValuesToEvaluator());
        System.out.println(Arrays.toString(bestParams));

    }

    @Test
    public void tuneMobilityWeights() throws IOException {

        int[] initialParams = new int[] {
                -18, -14, -8, -4, 0, 4, 8, 12, 16, -26, -21, -16, -12, -8, -4, 0, 4, 8, 12, 16, 16, 16, 16, -14, -12, -10, -8, -6, -4, -2, 0, 2, 4, 6, 8, 10, 12, 12, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 12, 12, -18, -14, -8, -4, 0, 4, 8, 12, 16, -26, -21, -16, -12, -8, -4, 0, 4, 8, 12, 16, 16, 16, 16, -14, -12, -10, -8, -6, -4, -2, 0, 2, 4, 6, 8, 10, 12, 12, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 12, 12
        };

        Function<int[], Evaluator> createEvaluatorFunction = (params) ->
        {
            EngineConfig config = EngineInitializer.loadDefaultConfig();
            config.getMiddlegameMobilityBonus()[1] = Arrays.stream(params, 0, 9).toArray();
            config.getMiddlegameMobilityBonus()[2] = Arrays.stream(params, 9, 23).toArray();
            config.getMiddlegameMobilityBonus()[3] = Arrays.stream(params, 23, 38).toArray();
            config.getMiddlegameMobilityBonus()[4] = Arrays.stream(params, 38, 66).toArray();
            config.getEndgameMobilityBonus()[1] = Arrays.stream(params, 66, 75).toArray();
            config.getEndgameMobilityBonus()[2] = Arrays.stream(params, 75, 89).toArray();
            config.getEndgameMobilityBonus()[3] = Arrays.stream(params, 89, 104).toArray();
            config.getEndgameMobilityBonus()[4] = Arrays.stream(params, 104, 132).toArray();
            return new Evaluator(config);
        };

        int[] bestParams = tuner.tune(initialParams, createEvaluatorFunction);
        System.out.println(Arrays.toString(bestParams));

    }

    @Test
    public void tunePieceValues() throws IOException {

        int[] initialParams = new int[] {
                82, 344, 365, 484, 1032, 0, 87, 280, 290, 519, 936, 0
        };
        Function<int[], Evaluator> createEvaluatorFunction = (params) ->
        {
            EngineConfig config = EngineInitializer.loadDefaultConfig();
            config.getPieceValues()[0] = Arrays.stream(params, 0, 6).toArray();
            config.getPieceValues()[1] = Arrays.stream(params, 6, 12).toArray();
            return new Evaluator(config);
        };

        int[] bestParams = tuner.tune(initialParams, createEvaluatorFunction);
        System.out.println(Arrays.toString(bestParams));

    }

    @Test
    public void testExtractPSTs() throws JsonProcessingException {

        int[] params = new int[] {
                0, 0, 0, 0, 0, 0, 0, 0, 273, -145, -436, 134, 371, 17, -79, -11, -228, 38, 49, 110, 144, 659, 40, -109, -53, 96, 49, 120, 164, -17, 45, -46, -180, 51, -4, 107, 128, 59, -61, -252, -165, -51, 79, -13, 99, -60, 216, -69, -124, 120, -58, -74, 19, 255, 357, -63, 0, 0, 0, 0, 0, 0, 0, 0, -808, -89, -34, 430, 1692, 30, -974, 644, 234, 774, 1391, 1091, 1286, 1533, 462, 766, 1016, 987, 940, 1264, 1851, 2008, 1294, 1147, 854, 896, 874, 1124, 964, 912, 849, 1045, 812, 851, 1007, 956, 1035, 862, 864, 633, 634, 712, 939, 889, 762, 880, 840, 635, 492, 716, 843, 804, 894, 961, 657, 748, 416, 644, 295, 544, 552, 517, 614, 532, 210, -683, -51, -486, 93, 232, 182, 151, -25, 543, 405, 874, 349, 1042, 625, 720, 210, 352, 762, 727, 954, 1265, 1700, 733, 339, 510, 734, 733, 704, 500, 520, 589, 251, 612, 524, 817, 689, 587, 417, 333, 671, 766, 590, 566, 597, 618, 553, 505, 197, 734, 393, 465, 534, 860, 768, 576, 742, 574, 521, 248, 356, 377, 476, 380, 143, 633, 191, 658, 1438, 2696, 32, 338, 90, -89, 348, 253, -65, 1290, 261, 137, 194, 18, 163, 451, 544, 764, 780, 799, -145, -11, -26, 545, 24, 490, 343, 339, -36, -51, -26, -194, 248, -16, 6, -40, 82, -25, 99, 118, -10, 163, 546, 318, -33, -183, -45, -58, -176, -6, 81, -360, 16, 204, 134, 17, 179, 118, -168, 87, -237, 879, 1564, 779, 1594, 1403, -534, 460, 27, 72, 166, -416, -161, 776, 555, 1297, 254, -17, 182, 495, 460, 975, 462, 758, 404, 122, 479, 191, 412, 340, 622, 424, 292, 121, 66, 237, 229, 261, 450, -3, -61, 318, 271, 273, 198, 277, 173, 198, 486, 259, 372, 313, 439, 526, 636, 56, 160, 391, 280, 379, 86, 408, -574, -50, -1093, 22, 16, -13, -305, -34, 1297, 10, -996, 230, 1467, 821, -369, 1179, -38, 378, -9, 24, -1023, -13, -20, 1053, 225, 169, -17, 1075, -13, -296, -1131, -25, 329, -287, 684, 719, 627, -626, -288, -429, -666, -51, -269, 97, -319, -597, -701, -391, -14, -232, -510, 7, -427, -663, -642, -305, 0, -18, -814, 36, 105, -781, -147, -419, 129, 31, 0, 0, 0, 0, 0, 0, 0, 0, 1345, 1340, 1341, 1069, 770, 1235, 1268, 1178, 973, 859, 744, 578, 561, 372, 625, 835, 419, 343, 292, 169, 123, 235, 324, 282, 262, 262, 144, 108, 80, 151, 240, 187, 242, 306, 99, 233, 143, 210, 157, 168, 272, 263, 279, 7, 244, 191, 154, 158, 0, 0, 0, 0, 0, 0, 0, 0, -91, 17, -222, 355, -80, 298, 392, -332, 454, 171, -158, -59, 174, 38, 327, 44, -143, 145, 441, 140, 158, 102, 332, -41, 286, 418, 469, 573, 557, 588, 399, 21, 301, 449, 391, 472, 415, 391, 419, 95, -62, 300, 270, 446, 521, 316, 299, 329, -57, -169, 199, 362, -2, 475, 249, 231, -316, 54, 386, 306, 139, 103, 47, 521, 513, 298, 252, 519, -7, 414, 570, 495, 337, 339, 334, 179, 460, 330, 387, 221, 401, 423, 311, 410, 461, 357, 31, 451, 532, 9, 387, 568, 493, 345, 266, 241, 209, 306, 484, 570, 510, 465, 364, 96, 267, 452, 391, 537, 516, 458, 454, 362, 545, 237, 332, 503, 387, 478, 336, 256, 138, 104, -23, 444, 360, 169, 270, -17, 784, 593, 525, 516, 443, 11, 535, 556, 538, 764, 588, 656, 752, 402, 635, 578, 622, 734, 604, 532, 403, 292, 378, 332, 627, 602, 660, 424, 649, 544, 350, 433, 514, 648, 455, 643, 450, 521, 383, 372, 539, 609, 506, 446, 616, 523, 103, 375, 593, 537, 655, 529, 480, 438, 596, 361, 526, 3, 522, 658, 442, 490, 485, 323, 1606, 1149, 589, 1214, -70, 754, 1897, 1587, 1330, 1275, 1327, 2128, 1561, 1372, 1341, 127, 907, 949, 1560, 1160, 1934, 1194, 1410, 472, 778, 1253, 775, 1636, 1176, 1351, 1328, 915, 845, 1339, 1858, 1374, 1478, 1121, 1526, 1446, 1159, 884, 1414, 1093, 1336, 1344, 1669, 1044, -22, 1456, 705, 1279, 855, 688, 283, 1597, 926, -28, 1049, 644, 1176, -95, 1531, 1430, -769, 54, -191, -221, -314, 196, -177, -509, 136, 252, -339, -354, 257, 0, 196, 70, -209, 17, 354, 6, 104, 60, 331, 122, -245, -95, 67, 248, 172, 180, 107, 114, -417, -191, -25, 287, 144, 230, 206, -58, -48, -45, 98, 214, 290, 241, 43, -9, -235, -20, 185, 207, 251, 161, 34, -76, -166, -217, -208, -10, -131, -101, -229, -348, 537, 1744, 2284, 3548, 7424, 0, 508, 1712, 1768, 3207, 5991, 0
        };
        EngineConfig config = EngineInitializer.loadDefaultConfig();
        config.getMiddlegameTables()[0] = Arrays.stream(params, 0, 64).toArray();
        config.getMiddlegameTables()[1] = Arrays.stream(params, 64, 128).toArray();
        config.getMiddlegameTables()[2] = Arrays.stream(params, 128, 192).toArray();
        config.getMiddlegameTables()[3] = Arrays.stream(params, 192, 256).toArray();
        config.getMiddlegameTables()[4] = Arrays.stream(params, 256, 320).toArray();
        config.getMiddlegameTables()[5] = Arrays.stream(params, 320, 384).toArray();
        config.getEndgameTables()[0] = Arrays.stream(params, 384, 448).toArray();
        config.getEndgameTables()[1] = Arrays.stream(params, 448, 512).toArray();
        config.getEndgameTables()[2] = Arrays.stream(params, 512, 576).toArray();
        config.getEndgameTables()[3] = Arrays.stream(params, 576, 640).toArray();
        config.getEndgameTables()[4] = Arrays.stream(params, 640, 704).toArray();
        config.getEndgameTables()[5] = Arrays.stream(params, 704, 768).toArray();
        config.getPieceValues()[0] = Arrays.stream(params, 768, 774).toArray();
        config.getPieceValues()[1] = Arrays.stream(params, 774, 780).toArray();
        System.out.println(new ObjectMapper().writeValueAsString(config));

    }

    private Function<int[], Evaluator> pstAndPieceValuesToEvaluator() {
        return (params) ->
        {
            if (params.length != 780) return null;
            EngineConfig config = EngineInitializer.loadDefaultConfig();
            config.getMiddlegameTables()[0] = Arrays.stream(params, 0, 64).toArray();
            config.getMiddlegameTables()[1] = Arrays.stream(params, 64, 128).toArray();
            config.getMiddlegameTables()[2] = Arrays.stream(params, 128, 192).toArray();
            config.getMiddlegameTables()[3] = Arrays.stream(params, 192, 256).toArray();
            config.getMiddlegameTables()[4] = Arrays.stream(params, 256, 320).toArray();
            config.getMiddlegameTables()[5] = Arrays.stream(params, 320, 384).toArray();
            config.getEndgameTables()[0] = Arrays.stream(params, 384, 448).toArray();
            config.getEndgameTables()[1] = Arrays.stream(params, 448, 512).toArray();
            config.getEndgameTables()[2] = Arrays.stream(params, 512, 576).toArray();
            config.getEndgameTables()[3] = Arrays.stream(params, 576, 640).toArray();
            config.getEndgameTables()[4] = Arrays.stream(params, 640, 704).toArray();
            config.getEndgameTables()[5] = Arrays.stream(params, 704, 768).toArray();
            config.getPieceValues()[0] = Arrays.stream(params, 768, 774).toArray();
            config.getPieceValues()[1] = Arrays.stream(params, 774, 780).toArray();
            return new Evaluator(config);
        };
    }

    @Test
    public void buildPieceValuePSTJson() throws JsonProcessingException {

        int[] params = new int[] {0, 0, 0, 0, 0, 0, 0, 0, -94, -97, -115, -89, -52, -38, -39, -169, -44, -44, 0, -44, 11, 135, 31, 5, -12, 5, 12, 17, 40, 62, 31, 0, -33, -13, -1, 21, 23, 37, 12, -24, -26, -16, 4, -2, 24, 16, 43, 4, -27, -1, -12, -10, 8, 51, 57, 1, 0, 0, 0, 0, 0, 0, 0, 0, -270, -89, -56, -77, 63, -112, -87, -119, -75, -25, 100, 25, 113, 93, 14, 25, -5, 21, 34, 73, 114, 139, 84, 19, 24, 25, 24, 68, 34, 58, 12, 45, 0, 9, 23, 22, 37, 37, 25, -10, -17, -3, 19, 19, 29, 16, 34, -14, -17, -24, 1, 5, 9, 19, 7, 7, -55, -17, -50, -21, -24, -10, -20, -69, -19, -55, -33, -84, -111, -34, -33, -29, -26, 10, -15, -5, 9, 67, 2, 50, -25, 9, 53, 6, 73, 62, 63, 45, -30, 1, 22, 38, 36, 4, 10, -1, -1, 9, 16, 42, 42, 10, 14, -14, 24, 32, 25, 15, 23, 29, 21, 27, -10, 36, 16, 3, 8, 40, 53, 16, 12, -7, 1, -14, -22, -10, -5, 4, 50, 60, 65, 80, 108, 182, 157, 50, -2, -15, 22, 61, 24, 100, 120, 66, 7, -3, 4, 30, 47, 82, 83, 63, -37, -10, 7, 29, 0, 24, 44, 40, -28, -35, -46, -25, -9, -2, 17, -9, -34, -24, -17, -9, -1, 7, 32, -4, -31, -17, -29, -18, -9, 4, 26, -59, -9, -9, -3, -1, 8, 8, -29, -3, -15, -5, 31, 60, 70, 96, 17, 5, -16, -38, -44, -73, -54, 91, 24, 116, -27, -24, 16, 29, 41, 86, 111, 72, -33, -21, -13, -10, -7, -3, -17, 15, -26, -26, -16, -20, 9, 3, 13, -8, -21, 3, -11, -6, -9, 5, 11, -9, -24, -2, 3, 4, 9, 20, 23, 3, -11, -19, -9, 5, -18, -35, -61, -5, -63, 95, 142, 98, 148, 83, 65, 87, 29, -1, 123, -7, 61, 6, -37, -36, 53, 177, 3, 93, -2, 61, 86, -38, -33, 25, 43, -7, -40, -23, -14, -42, -52, -2, -27, -62, -45, -103, -53, -118, 20, 17, -83, -77, -95, -79, -15, -49, 43, -13, -36, -51, -34, -52, 4, -5, -63, 39, 27, -23, 7, -25, 41, 12, 0, 0, 0, 0, 0, 0, 0, 0, 69, 60, 33, -12, -28, -12, 50, 64, 84, 69, 41, -2, -12, -5, 44, 46, 38, 26, 9, -16, -7, -4, 19, 14, 26, 19, 5, -8, 0, 1, 7, 7, 14, 10, 8, 2, 8, 14, -1, 0, 23, 7, 25, 14, 15, 14, -4, -1, 0, 0, 0, 0, 0, 0, 0, 0, -41, -68, -30, -21, -56, -52, -63, -124, -34, -13, -44, -19, -55, -43, -39, -67, -41, -23, 1, 1, -34, -25, -38, -39, -20, 5, 8, 21, 19, 11, 7, -25, -18, -9, 12, 14, 19, -2, -18, -14, -25, -13, -17, 9, 6, -7, -26, -40, -32, -29, -26, -9, -13, -19, -23, -47, -66, -52, -17, -13, -25, -14, -50, -40, -21, -38, -28, -9, -13, -15, -18, -18, -18, -14, -9, -20, -9, -28, -10, -53, 1, -13, 1, 2, -18, 1, -15, -20, 2, 5, 1, 3, 2, -11, -3, -18, -20, -11, 5, -9, 1, 2, -17, -26, -12, 1, -3, 9, 13, -3, -17, -24, -13, -29, -20, 1, 5, -12, -15, -30, -34, -18, -22, -11, -6, -22, -31, -26, 19, 13, 9, 2, -4, -21, -16, 11, 22, 38, 26, 7, 11, 5, -1, 4, 17, 20, 12, 3, 1, 0, 3, -7, 22, 10, 12, 1, 9, 8, -1, 1, 11, 13, 21, 17, 9, 6, 1, -4, 2, 0, 3, 1, 1, -7, -16, -19, 2, 2, 15, 5, 2, 5, -8, 9, 9, 7, 5, 9, -2, 13, 10, -20, -1, 27, 32, 36, 23, 20, 10, 54, -9, 28, 69, 134, 101, 87, 84, -2, -17, 21, 38, 27, 103, 42, 26, 6, -5, 46, 27, 67, 95, 106, 84, 46, 25, 51, 38, 82, 38, 34, 39, 20, -23, -34, 46, 29, 51, 32, 44, 6, 15, 19, -3, 16, 14, -25, -28, -23, 7, -21, -19, -44, 18, -6, -3, -53, -73, -37, -54, -31, -41, -20, -25, -56, -35, 8, -17, 7, 1, 38, 25, 5, -34, -13, 15, 3, 20, 32, 30, 14, -34, -4, 12, 26, 28, 36, 25, 9, -39, -7, 21, 34, 39, 45, 18, 6, -39, -6, 29, 39, 45, 40, 12, -7, -37, -3, 16, 28, 24, 30, 9, -14, -37, -33, -18, -18, -28, -13, -37, -50, 92, 393, 400, 544, 1119, 0, 78, 254, 280, 535, 1072, 0};

        EngineConfig config = EngineInitializer.loadDefaultConfig();
        config.getMiddlegameTables()[0] = Arrays.stream(params, 0, 64).toArray();
        config.getMiddlegameTables()[1] = Arrays.stream(params, 64, 128).toArray();
        config.getMiddlegameTables()[2] = Arrays.stream(params, 128, 192).toArray();
        config.getMiddlegameTables()[3] = Arrays.stream(params, 192, 256).toArray();
        config.getMiddlegameTables()[4] = Arrays.stream(params, 256, 320).toArray();
        config.getMiddlegameTables()[5] = Arrays.stream(params, 320, 384).toArray();
        config.getEndgameTables()[0] = Arrays.stream(params, 384, 448).toArray();
        config.getEndgameTables()[1] = Arrays.stream(params, 448, 512).toArray();
        config.getEndgameTables()[2] = Arrays.stream(params, 512, 576).toArray();
        config.getEndgameTables()[3] = Arrays.stream(params, 576, 640).toArray();
        config.getEndgameTables()[4] = Arrays.stream(params, 640, 704).toArray();
        config.getEndgameTables()[5] = Arrays.stream(params, 704, 768).toArray();
        config.getPieceValues()[0] = Arrays.stream(params, 768, 774).toArray();
        config.getPieceValues()[1] = Arrays.stream(params, 774, 780).toArray();

        System.out.println(new ObjectMapper().writeValueAsString(config));

    }

    @Test
    public void buildMobilityJson() throws JsonProcessingException {

        int[] paramsold = new int[] {
                -18, -14, -8, -4, 0, 4, 8, 12, 16, -26, -21, -16, -12, -8, -4, 0, 4, 8, 12, 16, 16, 16, 16, -14, -12, -10, -8, -6, -4, -2, 0, 2, 4, 6, 8, 10, 12, 12, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 12, 12, -18, -14, -8, -4, 0, 4, 8, 12, 16, -26, -21, -16, -12, -8, -4, 0, 4, 8, 12, 16, 16, 16, 16, -14, -12, -10, -8, -6, -4, -2, 0, 2, 4, 6, 8, 10, 12, 12, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 12, 12
        };
        int[] params = new int[] {
                -52, -19, -11, -7, -3, 5, 14, 19, 26, -37, -29, -15, -14, -6, 0, 7, 9, 12, 21, 21, 69, 15, 94, -93, -22, -6, -10, -4, -4, -5, 5, 4, 6, 14, 10, 15, 15, 8, -13, -155, -40, -7, -9, -19, -8, -10, -6, -4, -3, -2, 4, 4, 2, 3, -2, -3, -4, 6, 18, -8, 15, 75, 61, 33, 183, 11, -76, -37, -3, -3, 1, 10, 8, 11, -9, -69, -45, -43, -26, -11, 0, 7, 4, 14, 3, 10, -17, 23, -19, -87, -57, -19, -5, -10, -3, 4, -1, 2, 7, 8, 13, 13, 10, -1, -13, -245, -265, -248, -136, -107, -87, -61, -52, -39, -32, -19, -25, -13, -8, 4, 14, 21, 24, 20, 20, 35, 5, -33, -13, 12, -113, 12
        };

        EngineConfig config = EngineInitializer.loadDefaultConfig();
        config.getMiddlegameMobilityBonus()[1] = Arrays.stream(params, 0, 9).toArray();
        config.getMiddlegameMobilityBonus()[2] = Arrays.stream(params, 9, 23).toArray();
        config.getMiddlegameMobilityBonus()[3] = Arrays.stream(params, 23, 38).toArray();
        config.getMiddlegameMobilityBonus()[4] = Arrays.stream(params, 38, 66).toArray();
        config.getEndgameMobilityBonus()[1] = Arrays.stream(params, 66, 75).toArray();
        config.getEndgameMobilityBonus()[2] = Arrays.stream(params, 75, 89).toArray();
        config.getEndgameMobilityBonus()[3] = Arrays.stream(params, 89, 104).toArray();
        config.getEndgameMobilityBonus()[4] = Arrays.stream(params, 104, 132).toArray();

        System.out.println(new ObjectMapper().writeValueAsString(config));

    }

}