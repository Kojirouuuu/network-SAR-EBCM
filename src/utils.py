import numpy as np
import os

def save_results_np(save_dir, alpha_values, lambda_values, itr, t_range, aa_results, ab_results, a_results, r_results):
    """
    結果を指定されたディレクトリにCSVファイルとして保存する。

    Args:
        save_dir (str): 保存先のディレクトリ。
        alpha_values (list): alpha の値のリスト。
        lambda_values (list): lambda の値のリスト。
        itr (int): イテレーション数。
        t_range (int): 時間範囲。
        aa_results (ndarray): AAの結果配列。
        ab_results (ndarray): ABの結果配列。
        a_results (ndarray): Aの結果配列。
        r_results (ndarray): Rの結果配列。
    """
    shape = (len(alpha_values), len(lambda_values), itr, t_range + 1)

    # データサイズの確認
    expected = np.prod(shape)
    for data in [aa_results, ab_results, a_results, r_results]:
        if data.shape != shape:
            raise ValueError("データサイズが期待される形状と一致しません。")

    # 保存先のディレクトリが存在しない場合は作成
    os.makedirs(save_dir, exist_ok=True)

    # ファイル名とデータのマッピング
    results_data = {
        "aa_results.csv": aa_results,
        "ab_results.csv": ab_results,
        "a_results.csv": a_results,
        "r_results.csv": r_results
    }

    for filename, data in results_data.items():
        file_path = os.path.join(save_dir, filename)
        with open(file_path, "w") as f:
            # ヘッダー行を書き込む
            f.write("alpha,lambda,iteration,time,value\n")

            # 各パラメータの組み合わせに対してデータを書き込む
            for i, alpha in enumerate(alpha_values):
                for j, lamb in enumerate(lambda_values):
                    for k in range(itr):
                        for t in range(t_range + 1):
                            value = data[i, j, k, t]
                            f.write(f"{alpha},{lamb},{k},{t},{value}\n")

    print(f"CSVファイルの保存が完了しました。（保存先: {save_dir}）")



def align_to_max(time_series, ref_index):
    """
    各時系列データの最大値を取る位置を揃える。

    Args:
        time_series (ndarray): 1Dの時系列データ。
        ref_index (int): 最大値を揃える基準のインデックス。

    Returns:
        ndarray: 最大値がref_indexに揃えられた時系列データ。
    """
    max_index = np.argmax(time_series)
    shift = ref_index - max_index
    aligned = np.roll(time_series, shift)
    if shift > 0:
        aligned[:shift] = 0  # 前方を0で埋める
    elif shift < 0:
        aligned[shift:] = time_series[-1]  # 後方を最後の値で埋める
    return aligned