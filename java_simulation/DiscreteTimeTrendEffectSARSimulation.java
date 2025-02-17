package java_simulation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class DiscreteTimeTrendEffectSARSimulation {

    /** 内部クラス：グラフ情報を保持する */
    public static class Graph {
        public int[] edgeList;
        public int[] addressList;
        public int[] cursor;

        public Graph(int[] edgeList, int[] addressList, int[] cursor) {
            this.edgeList = edgeList;
            this.addressList = addressList;
            this.cursor = cursor;
        }
    }

    /**
     * ER(エルデシュ・レーニ)グラフを生成する。
     *
     * @param random       乱数生成器
     * @param numVertices  頂点数
     * @param averageDegree 平均次数
     * @return 生成したグラフ情報（Graph クラスのインスタンス）
     */
    private static Graph generateErdosRenyiGraph(Random random, int numVertices, double averageDegree) {
        int maxEdges = (int) Math.floor(numVertices * averageDegree);
        int[] rawEdgeList = new int[2 * maxEdges];
        Arrays.fill(rawEdgeList, -1);
        int[] vertexDegrees = new int[numVertices];
        int edgeCount = 0;

        // 重複エッジと自己ループを避けながら、エッジをランダムに選択
        Set<String> selectedEdges = new HashSet<>();
        int m = (int) (numVertices * averageDegree / 2);
        while (selectedEdges.size() < m) {
            int u = random.nextInt(numVertices);
            int v = random.nextInt(numVertices);
            if (u == v) continue;  // 自己ループを除外

            // 小さい方の頂点を先にしてエッジを一意に表現
            String edgeKey = (u < v) ? u + "-" + v : v + "-" + u;
            if (!selectedEdges.contains(edgeKey)) {
                selectedEdges.add(edgeKey);
                rawEdgeList[2 * edgeCount] = u;
                rawEdgeList[2 * edgeCount + 1] = v;
                vertexDegrees[u]++;
                vertexDegrees[v]++;
                edgeCount++;
            }
        }

        // -1 を除いたエッジリストに変換
        rawEdgeList = Arrays.stream(rawEdgeList).filter(x -> x != -1).toArray();

        // 隣接リスト構築用に addressList と cursor を初期化
        int[] addressList = new int[numVertices];
        int[] cursor = new int[numVertices];
        for (int vertex = 0; vertex < numVertices - 1; vertex++) {
            addressList[vertex + 1] = addressList[vertex] + vertexDegrees[vertex];
            cursor[vertex + 1] = cursor[vertex] + vertexDegrees[vertex];
        }

        // 実際の隣接リスト（edgeList）の作成
        int[] edgeList = new int[2 * edgeCount];
        for (int edgeIndex = 0; edgeIndex < edgeCount; edgeIndex++) {
            int u = rawEdgeList[2 * edgeIndex];
            int v = rawEdgeList[2 * edgeIndex + 1];
            int smaller = Math.min(u, v);
            int larger = Math.max(u, v);
            edgeList[cursor[smaller]] = larger;
            edgeList[cursor[larger]] = smaller;
            cursor[smaller]++;
            cursor[larger]++;
        }

        return new Graph(edgeList, addressList, cursor);
    }

    /**
     * 幅優先探索（BFS）により、グラフが連結かどうかを判定する。
     *
     * @param graph       グラフ情報
     * @param numVertices 頂点数
     * @return 連結なら true、そうでなければ false
     */
    private static boolean checkGraphConnectivity(Graph graph, int numVertices) {
        boolean[] visited = new boolean[numVertices];
        Queue<Integer> queue = new LinkedList<>();
        int startVertex = -1;

        // 隣接頂点を持つ頂点を起点として探索開始
        for (int i = 0; i < numVertices; i++) {
            if (graph.cursor[i] - graph.addressList[i] > 0) {
                startVertex = i;
                break;
            }
        }
        if (startVertex == -1) {
            return false;
        }

        visited[startVertex] = true;
        queue.add(startVertex);
        int visitedCount = 1;

        while (!queue.isEmpty()) {
            int current = queue.poll();
            for (int i = graph.addressList[current]; i < graph.cursor[current]; i++) {
                int neighbor = graph.edgeList[i];
                if (!visited[neighbor]) {
                    visited[neighbor] = true;
                    queue.add(neighbor);
                    visitedCount++;
                }
            }
        }
        return visitedCount == numVertices;
    }

    /**
     * シミュレーション引数を CSV に出力する。
     *
     * @param filename     出力先ファイル名
     * @param alphaValues  α の値の配列
     * @param lambdaValues λ の値の配列
     * @param iterations   反復回数（ネットワーク反復×シミュレーション反復）
     * @param maxTime      シミュレーションの最大時刻
     */
    private static void writeSimulationArgsCsv(String filename, double[] alphaValues, double[] lambdaValues,
                                                 int iterations, int maxTime) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("alphaValues,lambdaValues,iterations,maxTime");
            writer.newLine();
            writer.write(String.format("\"%s\",\"%s\",%d,%d",
                    Arrays.toString(alphaValues),
                    Arrays.toString(lambdaValues),
                    iterations, maxTime));
            writer.newLine();
        }
    }

    /**
     * シミュレーション結果（整数値データ）を CSV に出力する。
     * データは 4 次元配列 [αのインデックス][λのインデックス][反復][時刻] の順に書き出す。
     *
     * @param baseFilename   出力ファイルの基本名（例："s_all_results.csv"）
     * @param simulationData 4次元シミュレーション結果配列
     * @param alphaValues    α の値の配列
     * @param lambdaValues   λ の値の配列
     * @param iterations     反復回数
     * @param maxTime        シミュレーション最大時刻
     * @param batchNumber    バッチ番号（1～numBatches）
     */
    private static void writeSimulationResultCsv(String baseFilename, int[][][][] simulationData,
                                                  double[] alphaValues, double[] lambdaValues,
                                                  int iterations, int maxTime, int batchNumber) throws IOException {
        int numAlpha = alphaValues.length;
        int numLambda = lambdaValues.length;

        String filename = baseFilename.replace(".csv", "") + batchNumber + ".csv";

        // 結果のcsvファイルが大きくなるので、容量に余裕のある保存先を指定すると良い。
        String outputPath = "simulation_results/" + filename;
        // String outputPath = "/Users/username/Downloads/dir/" + filename;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("value");
            writer.newLine();

            // 外側から順に、α、λ、反復、時刻の順で出力
            for (int alphaIdx = 0; alphaIdx < numAlpha; alphaIdx++) {
                for (int lambdaIdx = 0; lambdaIdx < numLambda; lambdaIdx++) {
                    for (int iter = 0; iter < iterations; iter++) {
                        for (int time = 0; time <= maxTime; time++) {
                            writer.write(String.format("%d", simulationData[alphaIdx][lambdaIdx][iter][time]));
                            writer.newLine();
                        }
                    }
                }
            }
        }
    }

    /**
     * シミュレーションパラメータの詳細情報を CSV に出力する。
     *
     * @param filename         出力先ファイル名
     * @param graphType        ネットワーク種別（例："ER"）
     * @param numVertices      頂点数
     * @param averageDegree    平均次数
     * @param lambdaValues     λ の値の配列
     * @param alphaValues      α の値の配列
     * @param maxTime          シミュレーションの最大時刻
     * @param iterations       反復回数（ネットワーク反復×シミュレーション反復）
     * @param p                アクティビスト選出の確率
     * @param thresholdPair    閾値のペア（例：[1, 4]）
     * @param initialAdoptionRate 初期採用率
     * @param gamma            採用状態からの離脱確率
     */
    private static void writeSimulationParametersCsv(String filename, String graphType, int numVertices, double averageDegree,
                                                       double[] lambdaValues, double[] alphaValues, int maxTime, int iterations,
                                                       double p, int[] thresholdPair, double initialAdoptionRate, double gamma)
            throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("Parameter,Type,Value");
            writer.newLine();
            writeSingleParameter(writer, "Network", graphType);
            writeSingleParameter(writer, "numVertices", numVertices);
            writeSingleParameter(writer, "averageDegree", averageDegree);
            writeSingleParameter(writer, "maxTime", maxTime);
            writeSingleParameter(writer, "iterations", iterations);
            writeSingleParameter(writer, "p", p);
            writeSingleParameter(writer, "thresholdPair", Arrays.toString(thresholdPair));
            writeSingleParameter(writer, "initialAdoptionRate", initialAdoptionRate);
            writeSingleParameter(writer, "gamma", gamma);
            writeSingleParameter(writer, "lambdaValues", Arrays.toString(lambdaValues));
            writeSingleParameter(writer, "alphaValues", Arrays.toString(alphaValues));
        }
    }

    /** ヘルパーメソッド：パラメータ1件を書き出す */
    private static void writeSingleParameter(BufferedWriter writer, String parameterName, Object value) throws IOException {
        String typeName = value.getClass().getSimpleName();
        writer.write(parameterName + "," + typeName + "," + value);
        writer.newLine();
    }

    /**
     * パラメータを動かすようにする
     *
     * @param a 下限
     * @param b 上限（配列には含まれない）
     * @param d 刻み幅
     * @return パラメータの数値の配列
     */
    public static double[] arange(double a, double b, double d) {
		ArrayList<Double> arrayValues = new ArrayList<>();
		for (double mu = a; mu < b; mu+=d) {
			arrayValues.add(mu);
		}
		
		double[] listValues = new double[arrayValues.size()];
		for (int i = 0; i < arrayValues.size(); i++) {
			listValues[i] = arrayValues.get(i);
		}
		return listValues;
	}

    public static void main(String[] args) throws IOException {
        long startTime = System.nanoTime();

        // -------------------- グラフ生成パラメータ --------------------
        String graphType = "ER";
        int numVertices = 10000;
        double averageDegree = 10;

        // -------------------- シミュレーションパラメータ --------------------
        int maxTime = 100;
        int networkIterationCount = 1;
        int simulationIterationCount = 20;
        int totalIterations = networkIterationCount * simulationIterationCount;
        double initialAdoptionRate = 1.0 / numVertices;
        double gamma = 1.0;
        double p = 0.2;
        int[] thresholdPair = {1, 4};  // 例：1 または 4

        // α, λ の値を生成（例：0〜1.1 を 0.01 刻み）
        double lambdaStep = 0.01;
        double[] lambdaValues = arange(0, 1.0 + lambdaStep, lambdaStep);
        double alphaStep = 0.01;
        double[] alphaValues = arange(0, 1.1 + alphaStep, alphaStep);

        // シミュレーションパラメータを CSV に出力
        writeSimulationParametersCsv("parameters.csv", graphType, numVertices, averageDegree,
                lambdaValues, alphaValues, maxTime, totalIterations, p, thresholdPair, initialAdoptionRate, gamma);
        writeSimulationArgsCsv("args.csv", alphaValues, lambdaValues, totalIterations, maxTime);

        // -------------------- シミュレーション --------------------
        Random random = new Random();
        int numAlpha = alphaValues.length;
        int numLambda = lambdaValues.length;

        int numBatches = 5;
        // 各シミュレーション結果を格納する 4 次元配列（サイズ：[α][λ][反復][時刻=0～maxTime]）
        int[][][][] sAll = new int[numAlpha][numLambda][totalIterations][maxTime + 1];
        int[][][][] aaAll = new int[numAlpha][numLambda][totalIterations][maxTime + 1];
        int[][][][] abAll = new int[numAlpha][numLambda][totalIterations][maxTime + 1];
        int[][][][] raAll = new int[numAlpha][numLambda][totalIterations][maxTime + 1];
        int[][][][] rbAll = new int[numAlpha][numLambda][totalIterations][maxTime + 1];

        // バッチごとにシミュレーションを実行
        for (int batch = 1; batch <= numBatches; batch++) {
            for (int alphaIdx = 0; alphaIdx < numAlpha; alphaIdx++) {
                double currentAlpha = alphaValues[alphaIdx];
                if (alphaIdx % 20 == 0) {
                    System.out.println("Processing alpha: " + currentAlpha);
                    long elapsedTime = System.nanoTime() - startTime;
                    long seconds = (elapsedTime / 1_000_000_000) % 60;
                    long minutes = (elapsedTime / 1_000_000_000 / 60) % 60;
                    long hours = (elapsedTime / 1_000_000_000) / 3600;
                    System.out.printf("Elapsed Time: %d hours %d minutes %d seconds%n", hours, minutes, seconds);
                }
                for (int lambdaIdx = 0; lambdaIdx < numLambda; lambdaIdx++) {
                    double currentLambda = lambdaValues[lambdaIdx];
                    if (alphaIdx % 20 == 0 && lambdaIdx % 20 == 0) {
                        System.out.println("  Processing lambda: " + currentLambda);
                    }

                    // 各ネットワーク反復に対してシミュレーション実行
                    for (int netIter = 0; netIter < networkIterationCount; netIter++) {
                        // 連結グラフになるまでグラフを生成
                        Graph graph;
                        do {
                            graph = generateErdosRenyiGraph(random, numVertices, averageDegree);
                        } while (!checkGraphConnectivity(graph, numVertices));

                        int[] edgeList = graph.edgeList;
                        int[] addressList = graph.addressList;
                        int[] cursor = graph.cursor;

                        // 同一ネットワーク上で複数回シミュレーション実行
                        for (int simIter = 0; simIter < simulationIterationCount; simIter++) {
                            int iterationIndex = netIter * simulationIterationCount + simIter;

                            // 全ノードの初期閾値は thresholdPair[1] に設定
                            int[] nodeThresholds = new int[numVertices];
                            Arrays.fill(nodeThresholds, thresholdPair[1]);

                            // アクティビストの選出（p の割合）
                            int numActivists = (int) (p * numVertices);
                            List<Integer> nodeIndices = new ArrayList<>();
                            for (int v = 0; v < numVertices; v++) {
                                nodeIndices.add(v);
                            }
                            Collections.shuffle(nodeIndices);
                            List<Integer> activists = new ArrayList<>(nodeIndices.subList(0, numActivists));
                            for (int node : activists) {
                                nodeThresholds[node] = thresholdPair[0];
                            }

                            // 状態変数の記録リスト（0: Susceptible, 1: AdoptedA, 2: AdoptedB, 3: RecoveredA, 4: RecoveredB）
                            List<Integer> susceptibleList = new ArrayList<>();
                            List<Integer> adoptedAList = new ArrayList<>();
                            List<Integer> adoptedBList = new ArrayList<>();
                            List<Integer> recoveredAList = new ArrayList<>();
                            List<Integer> recoveredBList = new ArrayList<>();

                            int[] nodeStates = new int[numVertices];
                            int currentAdoptedA = 0;
                            int currentAdoptedB = 0;

                            // 初期採用者の設定
                            int initialAdopters = (int) (initialAdoptionRate * numVertices);
                            Collections.shuffle(nodeIndices);
                            List<Integer> initialAdoptersList = new ArrayList<>(nodeIndices.subList(0, initialAdopters));
                            for (int node : initialAdoptersList) {
                                if (nodeThresholds[node] == thresholdPair[0]) {
                                    nodeStates[node] = 1;
                                    currentAdoptedA++;
                                } else {
                                    nodeStates[node] = 2;
                                    currentAdoptedB++;
                                }
                            }

                            susceptibleList.add(numVertices - initialAdopters);
                            adoptedAList.add(currentAdoptedA);
                            adoptedBList.add(currentAdoptedB);
                            recoveredAList.add(0);
                            recoveredBList.add(0);

                            // 各ノードに情報を伝達した隣接ノードを記録するリスト
                            List<Set<Integer>> informedNeighbors = new ArrayList<>();
                            for (int i = 0; i < numVertices; i++) {
                                informedNeighbors.add(new HashSet<>());
                            }

                            int timeStep = 0;
                            int totalAdopted = currentAdoptedA + currentAdoptedB;

                            // 時刻発展ループ
                            while ((currentAdoptedA != 0 || currentAdoptedB != 0) && timeStep <= maxTime) {
                                Set<Integer> susceptibleToAdoptA = new HashSet<>();
                                Set<Integer> susceptibleToAdoptB = new HashSet<>();
                                Set<Integer> adoptedToRecoveredA = new HashSet<>();
                                Set<Integer> adoptedToRecoveredB = new HashSet<>();

                                for (int node = 0; node < numVertices; node++) {
                                    if (nodeStates[node] == 0) {
                                        if (random.nextDouble() < currentAlpha * totalAdopted / numVertices) {
                                            if (nodeThresholds[node] == thresholdPair[0]) {
                                                susceptibleToAdoptA.add(node);
                                            } else {
                                                susceptibleToAdoptB.add(node);
                                            }
                                        }
                                    } else if (nodeStates[node] == 1 || nodeStates[node] == 2) {
                                        // 隣接ノードへ影響伝達
                                        for (int i = addressList[node]; i < cursor[node]; i++) {
                                            int neighbor = edgeList[i];
                                            if (nodeStates[neighbor] == 0) {
                                                informedNeighbors.get(neighbor).add(node);
                                                if (informedNeighbors.get(neighbor).size() > nodeThresholds[neighbor]) {
                                                    if (nodeThresholds[neighbor] == thresholdPair[0]) {
                                                        susceptibleToAdoptA.add(neighbor);
                                                    } else {
                                                        susceptibleToAdoptB.add(neighbor);
                                                    }
                                                }
                                            }
                                        }
                                        // γ の確率で採用状態から回復
                                        if (random.nextDouble() < gamma) {
                                            if (nodeStates[node] == 1) {
                                                adoptedToRecoveredA.add(node);
                                            } else {
                                                adoptedToRecoveredB.add(node);
                                            }
                                        }
                                    }
                                }

                                // 状態更新
                                for (int node : susceptibleToAdoptA) {
                                    if (nodeStates[node] == 0) {
                                        nodeStates[node] = 1;
                                        currentAdoptedA++;
                                    }
                                }
                                for (int node : susceptibleToAdoptB) {
                                    if (nodeStates[node] == 0) {
                                        nodeStates[node] = 2;
                                        currentAdoptedB++;
                                    }
                                }
                                for (int node : adoptedToRecoveredA) {
                                    if (nodeStates[node] == 1) {
                                        nodeStates[node] = 3;
                                        currentAdoptedA--;
                                    }
                                }
                                for (int node : adoptedToRecoveredB) {
                                    if (nodeStates[node] == 2) {
                                        nodeStates[node] = 4;
                                        currentAdoptedB--;
                                    }
                                }

                                int susceptibleCount = susceptibleList.get(susceptibleList.size() - 1)
                                        - susceptibleToAdoptA.size() - susceptibleToAdoptB.size();
                                int adoptedACount = adoptedAList.get(adoptedAList.size() - 1)
                                        + susceptibleToAdoptA.size() - adoptedToRecoveredA.size();
                                int adoptedBCount = adoptedBList.get(adoptedBList.size() - 1)
                                        + susceptibleToAdoptB.size() - adoptedToRecoveredB.size();
                                int recoveredACount = recoveredAList.get(recoveredAList.size() - 1)
                                        + adoptedToRecoveredA.size();
                                int recoveredBCount = recoveredBList.get(recoveredBList.size() - 1)
                                        + adoptedToRecoveredB.size();

                                susceptibleList.add(susceptibleCount);
                                adoptedAList.add(adoptedACount);
                                adoptedBList.add(adoptedBCount);
                                recoveredAList.add(recoveredACount);
                                recoveredBList.add(recoveredBCount);

                                timeStep++;
                                totalAdopted = currentAdoptedA + currentAdoptedB;
                            }

                            // シミュレーションが途中で終了した場合、maxTime まで最終値で埋める
                            while (susceptibleList.size() <= maxTime) {
                                susceptibleList.add(susceptibleList.get(susceptibleList.size() - 1));
                                adoptedAList.add(adoptedAList.get(adoptedAList.size() - 1));
                                adoptedBList.add(adoptedBList.get(adoptedBList.size() - 1));
                                recoveredAList.add(recoveredAList.get(recoveredAList.size() - 1));
                                recoveredBList.add(recoveredBList.get(recoveredBList.size() - 1));
                            }

                            // 結果を記録
                            for (int t = 0; t <= maxTime; t++) {
                                sAll[alphaIdx][lambdaIdx][iterationIndex][t] = susceptibleList.get(t);
                                aaAll[alphaIdx][lambdaIdx][iterationIndex][t] = adoptedAList.get(t);
                                abAll[alphaIdx][lambdaIdx][iterationIndex][t] = adoptedBList.get(t);
                                raAll[alphaIdx][lambdaIdx][iterationIndex][t] = recoveredAList.get(t);
                                rbAll[alphaIdx][lambdaIdx][iterationIndex][t] = recoveredBList.get(t);
                            }
                        } // end simulationIteration
                    } // end networkIteration
                } // end lambda loop
            } // end alpha loop

            // CSV への出力（各バッチ毎にファイルを作成）
            writeSimulationResultCsv("s_all_results.csv", sAll, alphaValues, lambdaValues, totalIterations, maxTime, batch);
            writeSimulationResultCsv("aa_all_results.csv", aaAll, alphaValues, lambdaValues, totalIterations, maxTime, batch);
            writeSimulationResultCsv("ab_all_results.csv", abAll, alphaValues, lambdaValues, totalIterations, maxTime, batch);
            writeSimulationResultCsv("ra_all_results.csv", raAll, alphaValues, lambdaValues, totalIterations, maxTime, batch);
            writeSimulationResultCsv("rb_all_results.csv", rbAll, alphaValues, lambdaValues, totalIterations, maxTime, batch);
        }

        long endTime = System.nanoTime();
        long totalSeconds = (endTime - startTime) / 1_000_000_000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        System.out.printf("Total Execution Time: %d hours %d minutes %d seconds%n", hours, minutes, seconds);
    }
}
