# network-SAR-EBCM
Python を用いてネットワーク上の SAR (Susceptible–Affected–Recovered) モデルをシミュレーションし、エッジベースの区画理論と比較することで、情報拡散のダイナミクスを可視化・解析する研究プロジェクトです。

Overview
SARモデルをネットワーク上で実装し、頂点（ノード）の状態遷移を追跡します。
エッジベースの区画理論（Edge-based Compartment Model）と比較することで、理論とシミュレーションの違い・一致度を評価し、情報拡散や感染拡大のメカニズムを考察します。
小規模から中規模ネットワーク（ランダムグラフ、スケールフリー、スモールワールドなど）を対象にシミュレーションを行い、各種パラメータが拡散速度や最終的な到達率に与える影響を解析します。
Features
複数のネットワーク生成
networkx を用いたランダムグラフ（Erdős–Rényiモデル）
スケールフリーネットワーク（Barabási–Albertモデル）
スモールワールドネットワーク（Watts–Strogatzモデル）
SARモデルの柔軟なパラメータ設定
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
│   ├── main.py            # 実行用スクリプト
│   ├── model.py           # SARモデルのクラス・関数
│   ├── edge_based_model.py# エッジベース区画モデルのクラス・関数
│   ├── analysis.py        # 結果の分析・可視化用スクリプト
│   └── utils.py           # 補助的な関数（ネットワーク生成など）
├── notebooks/
│   ├── simulation_example.ipynb  # SARモデルの簡単なデモ
│   └── edge_based_comparison.ipynb # エッジベース理論比較のデモ
└── data/
    └── sample_networks/   # サンプルのネットワークデータ（必要に応じて）
src/: コアロジック（SARモデル、エッジベース理論、解析）を実装
notebooks/: 使い方や考察を示すためのJupyter Notebook
data/: 必要に応じてネットワークデータやサンプル入力ファイルを配置
requirements.txt: 本プロジェクトを動作させるために必要なPythonライブラリの一覧
Installation
リポジトリをクローンします。

bash
コピーする
編集する
git clone https://github.com/<YourUserName>/network-SAR-simulation.git
cd network-SAR-simulation
必要なライブラリをインストールします。
Anacondaなどの仮想環境を利用している場合は、環境を作成した上でインストールするとよいでしょう。

bash
コピーする
編集する
pip install -r requirements.txt
または、environment.yml がある場合は、

bash
コピーする
編集する
conda env create -f environment.yml
conda activate network-sar-env
インストールが完了したら、Jupyter Notebookを起動して動作確認を行います。

bash
コピーする
編集する
jupyter notebook
ブラウザで notebooks/ ディレクトリ内の .ipynb を開き、セルを順番に実行してください。

Usage
コマンドライン実行（例）
メインスクリプト: src/main.py
ネットワークタイプやモデルパラメータを引数で指定できます。

bash
コピーする
編集する
python src/main.py \
    --network_type scale_free \
    --num_nodes 1000 \
    --infection_rate 0.02 \
    --recovery_rate 0.01 \
    --initial_infected 10 \
    --steps 200
実行後は、拡散の推移がグラフとして出力されるほか、results/ フォルダにCSVや画像として結果が保存されます（設定に応じて）。

Jupyter Notebook での実行例
notebooks/simulation_example.ipynb
SARモデルの基本動作を確認するチュートリアルノートブック
ネットワーク生成 → シミュレーション → 可視化 → 簡単な考察
notebooks/edge_based_comparison.ipynb
エッジベースの区画理論とシミュレーション結果を比較し、感染率・拡散速度の違いを評価する
Results
下図はスケールフリーグラフ（ノード数1000、感染率0.02、回復率0.01）でのシミュレーション結果の一例です。
<img src="https://user-images.githubusercontent.com/xxx/graph_example.png" width="400" alt="Simulation Result">

青線がシミュレーションでの感染者割合、赤線がエッジベース区画モデルによる理論推定値。

実際のネットワーク構造依存や確率的要素により、理論とシミュレーションの間には誤差が生じますが、平均的には類似の挙動が得られることが確認できます。

License
このプロジェクトは MIT License の下で公開しています。詳細は LICENSE ファイルをご確認ください。

Contributing
本プロジェクトへのコントリビューションは歓迎です。
バグ報告や機能追加の提案があれば、Issue を立ててください。
Pull Request もお待ちしています。テストを追加してから送っていただけると助かります。
Contact / Author
Author: Your Name
Email: your-email@example.com
Twitter: @YourTwitterID
研究やプロジェクトでの利用、質問などがあれば上記の連絡先またはIssueにてお気軽にご連絡ください。

Future Work / TODO
ノードの異質性（異なる感染確率・回復確率）の考慮
マルチレイヤーネットワークでの拡散モデル適用
リアルタイム可視化のWebアプリ化（Streamlit や Dash の利用）
大規模ネットワーク（10万ノード規模）への拡張と高速化
