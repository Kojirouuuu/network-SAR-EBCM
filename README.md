# network-SAR-EBCM

Python を用いてネットワーク上の SAR (Susceptible–Adopted–Recovered) モデルをシミュレーションし、エッジベースの区画理論と比較することで、情報拡散のダイナミクスを可視化・解析する研究プロジェクトです。

Overview
SAR モデルをネットワーク上で実装し、頂点（ノード）の状態遷移を追跡します。
エッジベースの区画理論（Edge-based Compartment Model）と比較することで、理論とシミュレーションの違い・一致度を評価し、情報拡散や感染拡大のメカニズムを考察します。
小規模から中規模ネットワーク（ランダムグラフ、スケールフリー、スモールワールドなど）を対象にシミュレーションを行い、各種パラメータが拡散速度や最終的な到達率に与える影響を解析します。
Features
複数のネットワーク生成
networkx を用いたランダムグラフ（Erdős–Rényi モデル）
スケールフリーネットワーク（Barabási–Albert モデル）
スモールワールドネットワーク（Watts–Strogatz モデル）
SAR モデルの柔軟なパラメータ設定
感染率・回復率・情報拡散率を引数で変更可能
シミュレーションステップ数、初期感染（拡散）者数なども設定可能
可視化と比較解析
毎ステップの感染（拡散）状態の可視化
エッジベース区画理論との理論値比較
感染者数の時間変化、最終的な感染率、R0（基本再生産数）の推定
Directory Structure
bash
コピーする
編集する
network-SAR-simulation/
├── README.md
├── LICENSE
├── requirements.txt
├── src/
│ ├── main.py # 実行用スクリプト
│ ├── model.py # SAR モデルのクラス・関数
│ ├── edge_based_model.py# エッジベース区画モデルのクラス・関数
│ ├── analysis.py # 結果の分析・可視化用スクリプト
│ └── utils.py # 補助的な関数（ネットワーク生成など）
├── notebooks/
│ ├── simulation_example.ipynb # SAR モデルの簡単なデモ
│ └── edge_based_comparison.ipynb # エッジベース理論比較のデモ
└── data/
└── sample_networks/ # サンプルのネットワークデータ（必要に応じて）
src/: コアロジック（SAR モデル、エッジベース理論、解析）を実装
notebooks/: 使い方や考察を示すための Jupyter Notebook
data/: 必要に応じてネットワークデータやサンプル入力ファイルを配置
requirements.txt: 本プロジェクトを動作させるために必要な Python ライブラリの一覧
Installation
リポジトリをクローンします。

notebooks/analyze_from_java/analyze.ipynb
エッジベースの区画理論とシミュレーション結果を比較し、感染率・拡散速度の違いを評価する
Results
下図はスケールフリーグラフ（ノード数 1000、感染率 0.02、回復率 0.01）でのシミュレーション結果の一例です。
<img src="https://user-images.githubusercontent.com/xxx/graph_example.png" width="400" alt="Simulation Result">

青線がシミュレーションでの感染者割合、赤線がエッジベース区画モデルによる理論推定値。

実際のネットワーク構造依存や確率的要素により、理論とシミュレーションの間には誤差が生じますが、平均的には類似の挙動が得られることが確認できます。

Future Work / TODO
ノードの異質性（異なる感染確率・回復確率）の考慮
マルチレイヤーネットワークでの拡散モデル適用
