# network-SAR-EBCM 仕様書

## 1. システム概要

### 1.1 目的

本システムは、ネットワーク上での情報拡散や感染症の伝播をシミュレーションし、理論モデルと比較分析するための研究用フレームワークです。

### 1.2 主要機能

1. SAR モデルのネットワークシミュレーション
2. エッジベース区画理論との比較分析
3. シミュレーション結果の可視化と解析

## 2. システム構成

### 2.1 ディレクトリ構造

```
network-SAR-EBCM/
├── src/                    # ソースコード
│   ├── sar_simulation.py   # SARモデルのシミュレーション実装
│   ├── ebcm.py            # エッジベース区画モデルの実装
│   └── utils.py           # ユーティリティ関数
├── notebooks/             # 分析用ノートブック
│   └── analyze_from_java_results/  # Java実装との比較分析
├── java_simulation/       # Java実装のシミュレーションコード
├── simulation_results/    # シミュレーション結果の保存先
└── requirements.txt       # 依存パッケージ一覧
```

### 2.2 主要モジュール

#### 2.2.1 SAR シミュレーションモジュール (sar_simulation.py)

- 機能：ネットワーク上での SAR モデルのシミュレーション実行
- 主要クラス/関数：
  - `initialize_simulation(V, rho0, p, t_pair)`: シミュレーションの初期化
  - `simulate_iteration(G, state, informed, threshold_list, ...)`: 1 ステップのシミュレーション実行

#### 2.2.2 エッジベース区画モデル (ebcm.py)

- 機能：理論モデルの実装と計算
- 主要クラス/関数：
  - エッジベース区画理論に基づく理論値の計算
  - シミュレーション結果との比較機能

#### 2.2.3 ユーティリティモジュール (utils.py)

- 機能：補助的な機能の提供
- 主要機能：
  - ネットワーク生成
  - データ処理
  - 共通ユーティリティ関数

#### 2.2.4 Java シミュレーションモジュール (DiscreteTimeTrendEffectSARSimulation.java)

- 機能：大規模ネットワークでの効率的な SAR シミュレーション実行
- 主要クラス/関数：
  - `Graph`クラス：効率的なグラフ表現（エッジリスト、アドレスリスト、カーソル）
  - `generateErdosRenyiGraph`：ER グラフの生成
  - `checkGraphConnectivity`：グラフの連結性チェック
  - `writeSimulationResultCsv`：シミュレーション結果の CSV 出力

#### 2.2.5.1 データ構造

- グラフ表現：
  - `edgeList`：隣接ノードのリスト
  - `addressList`：各ノードの隣接リスト開始位置
  - `cursor`：各ノードの隣接リスト終了位置

#### 2.2.5.2 シミュレーションフロー

1. グラフ生成
   - ER グラフの生成（指定された平均次数）
   - 連結性の確認
2. 初期状態設定
   - 閾値の設定（活動家と一般ノード）
   - 初期採用者の選定
3. 時系列シミュレーション
   - 流行効果による感染
   - 口コミによる情報拡散
   - 回復プロセス
4. 結果の出力
   - 活動家の採用者数
   - 偏屈家の採用者数
   - 回復者数

#### 2.2.5.3 パラメータ設定

- ネットワークパラメータ：
  - `numVertices`：ノード数（デフォルト：10000）
  - `averageDegree`：平均次数（デフォルト：10）
- シミュレーションパラメータ：
  - `maxTime`：最大シミュレーション時間
  - `networkIterationCount`：ネットワーク生成の反復回数
  - `simulationIterationCount`：各ネットワークでのシミュレーション回数
  - `initialAdoptionRate`：初期採用率
  - `gamma`：回復率
  - `p`：活動家の割合
  - `ta`, `tb`：閾値のペア

#### 2.2.5.4 出力形式

- パラメータファイル：
  - `parameters.csv`：シミュレーション設定
  - `args.csv`：α, λ の値の範囲
- 結果ファイル：
  - `aa_all_results_[batch].csv`：活動家の採用者数
  - `ab_all_results_[batch].csv`：偏屈家の採用者数
  - `r_all_results_[batch].csv`：回復者数

## 3. 技術仕様

### 3.1 開発環境

- 言語：
  - Python 3.x
  - Java 8 以上
- 主要ライブラリ：
  - Python:
    - NumPy: 数値計算
    - NetworkX: ネットワーク分析
    - Matplotlib: 可視化
    - Jupyter: 分析環境
  - Java:
    - 標準ライブラリのみ使用

### 3.2 ネットワークモデル

1. ランダムグラフ（Erdős–Rényi モデル）
   - パラメータ：ノード数、接続確率

### 3.3 シミュレーションパラメータ

- 基本パラメータ：
  - N: ノード数
  - rho0: 初期採用者割合
  - p: 活動家の割合
  - t_pair: 閾値のペア
- 動的パラメータ：
  - alpha: 感染率
  - lambda: 感染伝播率
  - gamma: 回復率

## 4. 使用方法

### 4.1 環境構築

```bash
# リポジトリのクローン
git clone https://github.com/Kojirouuuu/network-SAR-EBCM.git
cd network-SAR-EBCM

# 依存パッケージのインストール
pip install -r requirements.txt
```

### 4.2 シミュレーション実行

1. Python スクリプトからの実行

```python
from src.sar_simulation import initialize_simulation, simulate_iteration
# シミュレーションの実行コード
```

2. Jupyter Notebook からの実行

- `notebooks/analyze_from_java_results/analyze.ipynb`を参照

## 5. 出力仕様

### 5.1 シミュレーション結果

- 形式：CSV ファイル
- 保存先：`simulation_results/`
- 出力項目：
  - 時間ステップ
  - 各状態のノード数
  - 理論値との比較データ

### 5.2 可視化出力

- 形式：PNG/PDF
- 出力項目：
  - 時系列グラフ
  - ネットワーク構造図
  - 理論値との比較グラフ

## 6. 制限事項

- 計算時間：
  - ネットワークサイズとシミュレーションステップ数に比例
  - Java 実装は並列処理なしでも高速
- ファイルサイズ：
  - シミュレーション結果は大きなファイルサイズになる可能性あり
  - バッチ処理による分割出力に対応

## 7. 今後の開発予定

1. 機能拡張
   - ノードの異質性の実装
   - マルチレイヤーネットワーク対応
2. パフォーマンス改善
   - 並列計算の実装
   - メモリ使用量の最適化
3. 分析機能の強化
   - より詳細な統計分析
   - インタラクティブな可視化

## 8. 参考文献

[W. Wang, M. Tang, P. Shu, and Z. Wang “Dynamics of social contagions with heterogeneous
adoption thresholds: crossover phenomena in phase transition” New Journal of Physics, 18(2016) 013029.](https://example.com)
