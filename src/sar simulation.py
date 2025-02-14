def initialize_simulation(V, rho0, t_pair):
    """
    初期状態を設定する。

    Args:
        V (int): ノード数。
        rho0 (float): 初期採用者の割合。
        t_pair (tuple): 閾値のペア。

    Returns:
        ndarray: 初期状態配列。
        list of set: 各ノードの情報を保持するセット。
        ndarray: 初期のaa_simおよびab_sim配列。
    """
    # 閾値リストを初期化
    threshold_list = np.ones(V) * t_pair[1]
    threshold_list[:int(p_list[0] * V)] = t_pair[0]
    
    state = np.zeros(V)  # 0:Sl,1:Sh,2:Aa,3:Ab,4:Ra,5:Rb
    state[threshold_list == t_pair[1]] = 1  # Sh

    informed = [set() for _ in range(V)]
    initial_adopted = np.random.choice(V, int(V * rho0), replace=False)
    
    aa_sim = np.zeros(V)
    ab_sim = np.zeros(V)
    for node in initial_adopted:
        if state[node] == 0:
            state[node] = 2
            aa_sim += 1
        elif state[node] == 1:
            state[node] = 3
            ab_sim += 1
    
    return state, informed, threshold_list, aa_sim, ab_sim

def simulate_iteration(G, state, informed, threshold_list, alpha, lamb, gamma, V, t_range):
    """
    SIRシミュレーションの1イテレーションを実行する。

    Args:
        G (Graph): ネットワークグラフ。
        state (ndarray): 現在の状態配列。
        informed (list of set): 各ノードの情報セット。
        threshold_list (ndarray): 閾値リスト。
        alpha (float): 感染率。
        lamb (float): 感染伝播率。
        gamma (float): 回復率。
        V (int): ノード数。
        t_range (int): シミュレーションの時間範囲。

    Returns:
        tuple: aa_sim, ab_sim, a_sim, r_simの時系列データ。
    """
    aa_sim = np.zeros(t_range + 1)
    ab_sim = np.zeros(t_range + 1)
    a_sim = np.zeros(t_range + 1)
    r_sim = np.zeros(t_range + 1)

    fracA = (np.sum(state == 2) + np.sum(state == 3)) / V

    for current_time in range(t_range):
        to_aa, to_ab, to_ra, to_rb = set(), set(), set(), set()

        for node in range(V):
            rand = np.random.rand()
            if state[node] == 0 and rand < alpha * fracA / threshold_list[node]:
                to_aa.add(node)
            elif state[node] == 1 and rand < alpha * fracA / threshold_list[node]:
                to_ab.add(node)
            elif state[node] in [2, 3] and rand < gamma:
                if state[node] == 2:
                    to_ra.add(node)
                else:
                    to_rb.add(node)
                
                for neighbor in G.neighbors(node):
                    if state[neighbor] in [0, 1] and np.random.rand() < lamb and node not in informed[neighbor]:
                        informed[neighbor].add(node)
                        if threshold_list[neighbor] <= len(informed[neighbor]):
                            if state[neighbor] == 0:
                                to_aa.add(neighbor)
                            else:
                                to_ab.add(neighbor)

        aa_sim[current_time + 1] = aa_sim[current_time]
        ab_sim[current_time + 1] = ab_sim[current_time]
        r_sim[current_time + 1] = r_sim[current_time]

        for node in to_aa:
            state[node] = 2
            aa_sim[current_time + 1] += 1
        for node in to_ab:
            state[node] = 3
            ab_sim[current_time + 1] += 1
        for node in to_ra:
            state[node] = 4
            aa_sim[current_time + 1] -= 1
            r_sim[current_time + 1] += 1
        for node in to_rb:
            state[node] = 5
            ab_sim[current_time + 1] -= 1
            r_sim[current_time + 1] += 1

        a_sim[current_time] = aa_sim[current_time] + ab_sim[current_time]
        fracA = (np.sum(state == 2) + np.sum(state == 3)) / V

    return aa_sim, ab_sim, a_sim, r_sim
