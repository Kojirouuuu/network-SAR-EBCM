def calculate_dtheta(theta, q, lamb, gamma, rho0, alpha, k_vals, pk, z):
    xi_s = 0
    for k2 in k_vals:
        Theta_k_a = np.array([
            p * theta ** (k2 - 1)
        ])
        Theta_k_b = np.array([
            (1 - p) * comb(k2 - 1, m) * theta ** (k2 - m - 1) * (1 - theta) ** m
            for m in range(t_pair[1])
        ])
        
        xi_s += k2 * pk[k2] * (np.sum(Theta_k_a) + np.sum(Theta_k_b))
    xi_s *= (1 - rho0) * q / z
    dtheta = - lamb * (theta - xi_s) + gamma * (1 - theta) * (1 - lamb)
    return dtheta

def calculate_dsl(theta, dthetadt, q, dqdt, lamb, gamma, rho0, alpha, k_vals, pk, z):
    dq_phi_k = 0
    q_dphi_k = 0
    for k in k_vals:
        dq_phi_k_a = np.array([
            theta ** k
        ])
        q_dphi_k_a = np.array([
            k * theta ** (k - 1) * dthetadt
        ])
        dq_phi_k += pk[k] * np.sum(dq_phi_k_a)
        q_dphi_k += pk[k] * np.sum(q_dphi_k_a)
    dsl = (1 - rho0) * dqdt * dq_phi_k + (1 - rho0) * q * q_dphi_k
    return dsl

def calculate_dsh(theta, dthetadt, q, dqdt, lamb, gamma, rho0, alpha, k_vals, pk, z):
    dq_phi_k = 0
    q_dphi_k = 0
    if theta == 1:
        for k in k_vals:
            dq_phi_k_b = np.array([
            theta ** k
            ])
            q_dphi_k_b = np.array([
            k * dthetadt,
            - k * dthetadt
            ])
            dq_phi_k += pk[k] * np.sum(dq_phi_k_b)
            q_dphi_k += pk[k] * np.sum(q_dphi_k_b)
    else:
        for k in k_vals:
            dq_phi_k_b = np.array([
                comb(k, m) * (theta ** (k - m) * (1 - theta) ** m)
                            for m in range(t_pair[1])
            ])
            q_dphi_k_b = np.array([
                comb(k, m) * ((k - m) * theta ** (k - m - 1) * dthetadt * (1 - theta) ** m
                            - theta ** (k - m) * m * (1 - theta) ** (m - 1) * dthetadt)
                            for m in range(t_pair[1])
            ])
            dq_phi_k += pk[k] * np.sum(dq_phi_k_b)
            q_dphi_k += pk[k] * np.sum(q_dphi_k_b)
    dsh = (1 - rho0) * dqdt * dq_phi_k + (1 - rho0) * q * q_dphi_k
    return dsh

# 微分方程式
def sar_derivatives(y, t, args):
    theta, q, sl, sh, aa, ab, ra, rb = y
    a = p_list[0] * aa + p_list[1] * ab
    dthetadt = calculate_dtheta(theta, q, *args)
    dqdt = q * ((1 - alpha * a) - 1)
    dsldt = calculate_dsl(theta, dthetadt, q, dqdt, *args)
    dshdt = calculate_dsh(theta, dthetadt, q, dqdt, *args)
    daadt = - dsldt - gamma * aa
    dabdt = - dshdt - gamma * ab
    dradt = gamma * aa
    drbdt = gamma * ab
    return np.array([dthetadt, dqdt, dsldt, dshdt, daadt, dabdt, dradt, drbdt])

# ルンゲクッタ法 (RK4)
def runge_kutta4(func, y0, t, args=()):
    n = len(t)
    y = np.zeros((n, len(y0)))
    y[0] = y0
    for i in range(n - 1):
        h = t[i + 1] - t[i]
        k1 = func(y[i], t[i], args)
        k2 = func(y[i] + h * k1 / 2, t[i] + h / 2, args)
        k3 = func(y[i] + h * k2 / 2, t[i] + h / 2, args)
        k4 = func(y[i] + h * k3, t[i] + h, args)
        y[i + 1] = y[i] + h * (k1 + 2 * k2 + 2 * k3 + k4) / 6
    return y