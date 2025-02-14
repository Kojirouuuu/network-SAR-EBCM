def load_results_np(alpha_values, lambda_values, itr, t_range):
    """
    CSVファイルから結果をNumPy配列として読み込み、多次元配列に再構築する。

    Args:
        alpha_values (list): alphaの値のリスト。
        lambda_values (list): lambdaの値のリスト。
        itr (int): イテレーション数。
        t_range (int): 時間範囲。

    Returns:
        aa_results, ab_results, a_results, r_results (ndarray): 再構築された結果配列。
    """
    shape = (len(alpha_values), len(lambda_values), itr, t_range + 1)
    
    # 各CSVファイルから必要な列（結果列）のみを読み込む
    # usecols=4 は5番目の列（0始まり）を指します。0: alpha, 1: lambda, 2: iteration, 3: time, 4: 結果
    aa = np.loadtxt('aa_results.csv', delimiter=',', usecols=4, skiprows=1)
    ab = np.loadtxt('ab_results.csv', delimiter=',', usecols=4, skiprows=1)
    a = np.loadtxt('a_results.csv', delimiter=',', usecols=4, skiprows=1)
    r = np.loadtxt('r_results.csv', delimiter=',', usecols=4, skiprows=1)
    
    # データ数の確認
    expected = np.prod(shape)
    if aa.size != expected or ab.size != expected or a.size != expected or r.size != expected:
        raise ValueError("データサイズが期待される形状と一致しません。")
    
    # 配列の再構築
    aa_results = aa.reshape(shape)
    ab_results = ab.reshape(shape)
    a_results = a.reshape(shape)
    r_results = r.reshape(shape)
    
    return aa_results, ab_results, a_results, r_results

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