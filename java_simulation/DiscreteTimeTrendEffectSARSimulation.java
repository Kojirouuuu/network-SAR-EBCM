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
     */
    private static Graph generateErdosRenyiGraph(Random random, int numVertices, double averageDegree) {
        int maxEdges = (int) Math.floor(numVertices * averageDegree);
        int[] rawEdgeList = new int[2 * maxEdges];
        Arrays.fill(rawEdgeList, -1);
        int[] vertexDegrees = new int[numVertices];
        int edgeCount = 0;

        // 重複エッジと自己ループを避けながらエッジをランダムに選択
        Set<String> selectedEdges = new HashSet<>();
        int m = (int) (numVertices * averageDegree / 2);
        while (selectedEdges.size() < m) {
            int u = random.nextInt(numVertices);
            int v = random.nextInt(numVertices);
            if (u == v) continue;  // 自己ループ除外

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

        rawEdgeList = Arrays.stream(rawEdgeList).filter(x -> x != -1).toArray();

        int[] addressList = new int[numVertices];
        int[] cursor = new int[numVertices];
        for (int vertex = 0; vertex < numVertices - 1; vertex++) {
            addressList[vertex + 1] = addressList[vertex] + vertexDegrees[vertex];
            cursor[vertex + 1] = cursor[vertex] + vertexDegrees[vertex];
        }

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
     * 幅優先探索（BFS）によりグラフが連結かどうか判定
     */
    private static boolean checkGraphConnectivity(Graph graph, int numVertices) {
        boolean[] visited = new boolean[numVertices];
        Queue<Integer> queue = new LinkedList<>();
        int startVertex = -1;
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
     * シミュレーション引数をCSVに出力
     */
    private static void writeSimulationArgsCsv(String baseFilename, double[] alphaValues, double[] lambdaValues,
                                                 int iterations, int maxTime) throws IOException {
        String filename = baseFilename.replace(".csv", "") + ".csv";
        String outputPath = "simulation_results/" + filename;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
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
     * シミュレーション結果（4次元配列）をCSVに出力
     * データは [α][λ][反復][時刻] の順で書き出す。
     */
    private static void writeSimulationResultCsv(String baseFilename, int[][][][] simulationData,
                                                  double[] alphaValues, double[] lambdaValues,
                                                  int iterations, int maxTime, int batchNumber) throws IOException {
        int numAlpha = alphaValues.length;
        int numLambda = lambdaValues.length;
        String filename = baseFilename.replace(".csv", "") + "_" + batchNumber + ".csv";
        String outputPath = "simulation_results/" + filename;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("value");
            writer.newLine();
            for (int a = 0; a < numAlpha; a++) {
                for (int l = 0; l < numLambda; l++) {
                    for (int iter = 0; iter < iterations; iter++) {
                        for (int t = 0; t <= maxTime; t++) {
                            writer.write(String.format("%d", simulationData[a][l][iter][t]));
                            writer.newLine();
                        }
                    }
                }
            }
        }
    }

    /**
     * シミュレーションパラメータ詳細情報をCSVに出力
     */
    private static void writeSimulationParametersCsv(String baseFilename, String graphType, int numVertices, double averageDegree,
                                                       int maxTime, int numBatches, int iterations,
                                                       double p, int ta, int tb, double initialAdoptionRate, double gamma)
            throws IOException {
        String filename = baseFilename.replace(".csv", "") + ".csv";
        String outputPath = "simulation_results/" + filename;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("Parameter,Type,Value");
            writer.newLine();
            writeSingleParameter(writer, "Network", graphType);
            writeSingleParameter(writer, "numVertices", numVertices);
            writeSingleParameter(writer, "averageDegree", averageDegree);
            writeSingleParameter(writer, "maxTime", maxTime);
            writeSingleParameter(writer, "numBatches", numBatches);
            writeSingleParameter(writer, "iterations", iterations);
            writeSingleParameter(writer, "p", p);
            writeSingleParameter(writer, "ta", ta);
            writeSingleParameter(writer, "tb", tb);
            writeSingleParameter(writer, "initialAdoptionRate", initialAdoptionRate);
            writeSingleParameter(writer, "gamma", gamma);
        }
    }

    /** ヘルパーメソッド：パラメータ1件を書き出す */
    private static void writeSingleParameter(BufferedWriter writer, String parameterName, Object value) throws IOException {
        String typeName = value.getClass().getSimpleName();
        writer.write(parameterName + "," + typeName + "," + value);
        writer.newLine();
    }

    /**
     * パラメータを動かすためのメソッド
     */
    public static double[] arange(double a, double b, double d) {
        ArrayList<Double> arrayValues = new ArrayList<>();
        for (double mu = a; mu < b; mu += d) {
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
        int numBatches = 5;
        int networkIterationCount = 4;
        int simulationIterationCount = 50;
        int totalIterations = networkIterationCount * simulationIterationCount;
        double initialAdoptionRate = 1.0 / numVertices;
        double gamma = 1.0;
        double p = 0.2;
        int ta = 1;
        int tb = 4;
        int[] thresholdPair = {ta, tb};

        // α, λ の値（例：0〜1.1 を0.1刻み）
        double lambdaStep = 0.01;
        double[] lambdaValues = arange(0, 1.0, lambdaStep);
        double alphaStep = 0.01;
        double[] alphaValues = arange(0, 1.1, alphaStep);

        // シミュレーションパラメータをCSVに出力
        writeSimulationParametersCsv("parameters.csv", graphType, numVertices, averageDegree, maxTime, numBatches, totalIterations, p, ta, tb, initialAdoptionRate, gamma);
        writeSimulationArgsCsv("args.csv", alphaValues, lambdaValues, totalIterations, maxTime);

        Random random = new Random();
        int numAlpha = alphaValues.length;
        int numLambda = lambdaValues.length;

        // -------------------- バッチ単位でシミュレーション --------------------
        for (int batch = 1; batch <= numBatches; batch++) {

            // バッチごとに結果配列を新規作成
            int[][][][] aaResults = new int[numAlpha][numLambda][totalIterations][maxTime + 1];
            int[][][][] abResults = new int[numAlpha][numLambda][totalIterations][maxTime + 1];
            int[][][][] rResults  = new int[numAlpha][numLambda][totalIterations][maxTime + 1];

            for (int alphaIdx = 0; alphaIdx < numAlpha; alphaIdx++) {
                double currentAlpha = alphaValues[alphaIdx];
                if (alphaIdx % (int)(numAlpha / 10) == 0) {
                    System.out.printf("alpha: " + currentAlpha);
                    long elapsedTime = System.nanoTime() - startTime;
                    long seconds = (elapsedTime / 1_000_000_000) % 60;
                    long minutes = (elapsedTime / 1_000_000_000 / 60) % 60;
                    long hours = (elapsedTime / 1_000_000_000) / 3600;
                    System.out.printf(" Elapsed Time: %d hours %d minutes %d seconds%n", hours, minutes, seconds);
                }
                for (int lambdaIdx = 0; lambdaIdx < numLambda; lambdaIdx++) {
                    double currentLambda = lambdaValues[lambdaIdx];
                    if (alphaIdx % (int)(numAlpha / 10) == 0 && lambdaIdx % (int)(numLambda / 10) == 0) {
                        System.out.println("  --> lambda: " + currentLambda);
                    }

                    // ネットワーク反復
                    for (int netIter = 0; netIter < networkIterationCount; netIter++) {
                        // 連結グラフが得られるまで生成
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

                            // 初期閾値設定（すべてthresholdPair[1]で初期化）
                            int[] nodeThresholds = new int[numVertices];
                            Arrays.fill(nodeThresholds, thresholdPair[1]);

                            // アクティビストの選出（確率p）
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

                            // ノード状態の初期化
                            // 状態：0 = Susceptible, 1 = AdoptedA, 2 = AdoptedB, 3 = RecoveredA, 4 = RecoveredB
                            int[] nodeStates = new int[numVertices];
                            int currentAdoptedA = 0;
                            int currentAdoptedB = 0;

                            // 初期採用者の設定（初期採用率に基づきランダム選出）
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

                            // 時系列データリスト：各リストのインデックスが時刻 t (0～maxTime)
                            List<Integer> adoptedAList = new ArrayList<>();
                            List<Integer> adoptedBList = new ArrayList<>();
                            List<Integer> recoveredAList = new ArrayList<>();
                            List<Integer> recoveredBList = new ArrayList<>();
                            // 初期状態の記録
                            adoptedAList.add(currentAdoptedA);
                            adoptedBList.add(currentAdoptedB);
                            recoveredAList.add(0);
                            recoveredBList.add(0);

                            // 各ノードに伝達済みの隣接ノード集合を記録
                            List<Set<Integer>> informedNeighbors = new ArrayList<>();
                            for (int i = 0; i < numVertices; i++) {
                                informedNeighbors.add(new HashSet<>());
                            }

                            int timeStep = 0;
                            int totalAdopted = currentAdoptedA + currentAdoptedB;

                            // 固定ステップ数maxTimeだけシミュレーション実行
                            while (timeStep < maxTime) {
                                Set<Integer> susceptibleToAdoptA = new HashSet<>();
                                Set<Integer> susceptibleToAdoptB = new HashSet<>();
                                Set<Integer> adoptedToRecoveredA = new HashSet<>();
                                Set<Integer> adoptedToRecoveredB = new HashSet<>();

                                for (int node = 0; node < numVertices; node++) {
                                    if (nodeStates[node] == 0) {  // Susceptible
                                        if (random.nextDouble() < currentAlpha * totalAdopted / numVertices) {
                                            if (nodeThresholds[node] == thresholdPair[0]) {
                                                susceptibleToAdoptA.add(node);
                                            } else {
                                                susceptibleToAdoptB.add(node);
                                            }
                                        }
                                    } else if (nodeStates[node] == 1 || nodeStates[node] == 2) {  // Adopted状態
                                        for (int i = addressList[node]; i < cursor[node]; i++) {
                                            int neighbor = edgeList[i];
                                            if (nodeStates[neighbor] == 0 && random.nextDouble() < currentLambda) {
                                                informedNeighbors.get(neighbor).add(node);
                                                if (informedNeighbors.get(neighbor).size() >= nodeThresholds[neighbor]) {
                                                    if (nodeThresholds[neighbor] == thresholdPair[0]) {
                                                        susceptibleToAdoptA.add(neighbor);
                                                    } else {
                                                        susceptibleToAdoptB.add(neighbor);
                                                    }
                                                }
                                            }
                                        }
                                        // 採用状態から回復
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

                                // 次の時刻の記録
                                adoptedAList.add(currentAdoptedA);
                                adoptedBList.add(currentAdoptedB);
                                int prevRecoveredA = recoveredAList.get(recoveredAList.size() - 1);
                                int prevRecoveredB = recoveredBList.get(recoveredBList.size() - 1);
                                recoveredAList.add(prevRecoveredA + adoptedToRecoveredA.size());
                                recoveredBList.add(prevRecoveredB + adoptedToRecoveredB.size());

                                timeStep++;
                                totalAdopted = currentAdoptedA + currentAdoptedB;
                            } // end timeStep loop

                            // 結果を各時刻ごとに保存（時刻0～maxTime）
                            for (int t = 0; t <= maxTime; t++) {
                                int adoptedA = adoptedAList.get(t);
                                int adoptedB = adoptedBList.get(t);
                                aaResults[alphaIdx][lambdaIdx][iterationIndex][t] = adoptedA;
                                abResults[alphaIdx][lambdaIdx][iterationIndex][t] = adoptedB;
                                int recovA = recoveredAList.get(t);
                                int recovB = recoveredBList.get(t);
                                rResults[alphaIdx][lambdaIdx][iterationIndex][t] = recovA + recovB;
                            }
                        } // end simulationIteration loop
                    } // end networkIteration loop
                } // end lambda loop
            } // end alpha loop

            // 各バッチ毎にCSV出力（ファイル名にバッチ番号を付与）
            writeSimulationResultCsv("aa_all_results.csv", aaResults, alphaValues, lambdaValues, totalIterations, maxTime, batch);
            writeSimulationResultCsv("ab_all_results.csv", abResults, alphaValues, lambdaValues, totalIterations, maxTime, batch);
            writeSimulationResultCsv("r_all_results.csv", rResults, alphaValues, lambdaValues, totalIterations, maxTime, batch);
        } // end batch loop

        long endTime = System.nanoTime();
        long totalSeconds = (endTime - startTime) / 1_000_000_000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        System.out.printf("Total Execution Time: %d hours %d minutes %d seconds%n", hours, minutes, seconds);
    }
}
