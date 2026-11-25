# Звіт та інструкція для демонстрації: Observability, GitOps та Canary Rollback (`user-service`)

## Архітектура рішення
* **Кластер:** Minikube (`medical-system`, `monitoring`, `argocd`, `argo-rollouts`)
* **GitOps & Progressive Delivery:** Argo CD + Argo Rollouts (`AnalysisTemplate` для автоматичного відкату)
* **Метрики:** Spring Boot Actuator + Micrometer (`/actuator/prometheus`) → `ServiceMonitor` → Prometheus (`kube-prometheus-stack`)
* **Логи:** `ObservabilityFilter` (структурований JSON у `stdout`) → Grafana Alloy (`DaemonSet`) → Grafana Loki
* **Візуалізація:** Grafana Dashboard, доставлений декларативно через Git (`ConfigMap` із міткою `grafana_dashboard: "1"`)

---

## 1. Підготовка та запуск Port-Forward (відкрити в окремих вікнах PowerShell)

### 1.1. Перевірка стану подів та застосунків
```powershell
kubectl -n medical-system get pods -o custom-columns="POD:.metadata.name,IMAGE:.spec.containers[0].image,STATUS:.status.phase"
kubectl -n monitoring get pods
kubectl -n argocd get app

```

### 1.2. Запуск доступу до веб-інтерфейсів

* **Prometheus (`http://localhost:9090`):**
```powershell
kubectl -n monitoring port-forward svc/kps-prometheus 9090:9090

```


* **Grafana (`http://localhost:3000`):**
```powershell
kubectl -n monitoring port-forward svc/kps-grafana 3000:80

```


*Отримання пароля адміністратора Grafana (логін `admin`):*
```powershell
[System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String((kubectl -n monitoring get secret kps-grafana -o jsonpath="{.data.admin-password}")))

```


* **Argo CD (`https://localhost:8080`):**
```powershell
kubectl -n argocd port-forward svc/argocd-server 8080:443

```


*Отримання пароля адміністратора Argo CD (логін `admin`):*
```powershell
[System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String((kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}")))

```


* **Сервіс `user-service` (`http://localhost:8082`):**
```powershell
kubectl -n medical-system port-forward svc/user-service 8082:8082

```



---

## 2. Перевірка збору метрик (Prometheus) та логів (Loki)

### 2.1. Перевірка Prometheus

1. Відкрити `http://localhost:9090/targets` → переконатися, що таргет `serviceMonitor/medical-system/user-service/0` має статус **UP**.
2. Перевірити ендпоінт метрик напряму:
```powershell
curl.exe http://localhost:8082/actuator/prometheus | Select-String "http_server_requests"

```


3. Виконати PromQL-запит у вкладці **Graph** (`http://localhost:9090/graph`):
```promql
sum by (status) (rate(http_server_requests_seconds_count{namespace="medical-system"}[1m]))

```



### 2.2. Перевірка JSON-логів у Loki

1. Згенерувати кілька тестових запитів:
```powershell
1..5 | ForEach-Object { curl.exe -s -o NUL http://localhost:8082/api/v1/users }

```


2. Відкрити **Grafana (`http://localhost:3000`) → Explore → Loki** та виконати запити:
* Усі логи сервісу:
```logql
{namespace="medical-system"}

```


* Фільтрація помилок рівня `5xx` за полями JSON:
```logql
{namespace="medical-system"} | json | status >= 500

```





---

## 3. Панелі дашборда Grafana (GitOps)

Дашборд зберігається у `gitops/charts/user-service/dashboards/user-service.json` та розгортається через `gitops/charts/user-service/templates/dashboard.yaml`.

| Назва панелі | Джерело | Запит (PromQL / LogQL) |
| --- | --- | --- |
| **Запити за секунду (RPS)** | Prometheus | `sum(rate(http_server_requests_seconds_count{namespace="medical-system", uri!="/actuator/prometheus"}[1m]))` |
| **Частка помилок 5xx** | Prometheus | `sum(rate(http_server_requests_seconds_count{namespace="medical-system", status=~"5..", uri!="/actuator/prometheus"}[1m])) / sum(rate(http_server_requests_seconds_count{namespace="medical-system", uri!="/actuator/prometheus"}[1m]))` |
| **p95 затримки (Latency)** | Prometheus | `histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{namespace="medical-system", uri!="/actuator/prometheus"}[1m])))` |
| **Помилки 5xx по подах** | Prometheus | `sum by (pod) (rate(http_server_requests_seconds_count{namespace="medical-system", status=~"5..", uri!="/actuator/prometheus"}[1m]))` |
| **CPU подів** | Prometheus | `sum by (pod) (rate(container_cpu_usage_seconds_total{namespace="medical-system", container!=""}[1m]))` |
| **Пам'ять подів (RAM)** | Prometheus | `sum by (pod) (container_memory_working_set_bytes{namespace="medical-system", container!=""})` |
| **Логи помилок** | Loki | `{namespace="medical-system"} | json | level="error"` |

---

## 4. Навантажувальний тест та демонстрація масштабування

Перед запуском у Grafana виставити діапазон **Last 15 minutes** та автооновлення **10s**.

### 4.1. Запуск постійного фонового трафіку

```powershell
kubectl create deployment load --image=curlimages/curl -- sh -c "while true; do curl -s -o /dev/null [http://user-service.medical-system.svc:8082/api/v1/users](http://user-service.medical-system.svc:8082/api/v1/users); sleep 0.05; done"

```

### 4.2. Пікове навантаження (`hey`)

```powershell
kubectl run hey --rm -it --restart=Never --image=williamyeh/hey -- -z 2m -c 20 [http://user-service.medical-system.svc:8082/api/v1/users](http://user-service.medical-system.svc:8082/api/v1/users)

```

*Результат на дашборді:* різке зростання **RPS**, збільшення **p95 latency** та навантаження **CPU** на поди, після чого повернення до базового рівня.

### 4.3. Додаткова зміна: масштабування реплік через Git (`3 → 5`)

1. У файлі `gitops/charts/user-service/values-minikube.yaml` змінити `replicaCount: 5`.
2. Запушити зміни:
```powershell
git add gitops/charts/user-service/values-minikube.yaml
git commit -m "Scale user-service replicas to 5"
git push

```


3. *Результат на дашборді:* поява двох нових ліній на панелях **CPU** та **Пам'ять подів**, зниження навантаження CPU на кожен окремий под.

---

## 5. Автоматичний відкат за метриками (`AnalysisTemplate`)

> **Важливо:** фоновий генератор `deployment/load` має бути активним, щоб Prometheus отримував запити для обчислення `success-rate`.

### 5.1. Запуск спостереження за Canary Rollout

```powershell
kubectl argo rollouts get rollout user-service -n medical-system --watch

```

### 5.2. Внесення «зламаної» конфігурації (`FAIL_RATE = 0.5`)

1. У `gitops/charts/user-service/values-minikube.yaml` змінити:
```yaml
env:
  APP_VERSION: "1.3.1-broken"
  FAIL_RATE: "0.5"

```


2. Закомітити та запушити:
```powershell
git add gitops/charts/user-service/values-minikube.yaml
git commit -m "Introduce 50% error rate in v1.3.1-broken"
git push

```



### 5.3. Спостережуваний ефект

1. **Argo Rollouts:** створюється Canary-под (`20%` трафіку), запускається `AnalysisRun`. Через падіння `success-rate` нижче `0.95` перевірка переходить у **Failed**, і реліз автоматично відкочується (**Degraded / Rollback**).
2. **Grafana (метрики):** на панелі **«Помилки 5xx по подах»** та **«Частка помилок 5xx»** з'являється сплеск помилок від нового пода, який зникає після відкату.
3. **Grafana (Loki):** на панелі логів з'являються записи зі статусом `500` та версією `"version":"1.3.1-broken"`, тоді як стабільні поди `1.3.0` продовжують працювати без 500-х помилок.

### 5.4. Повернення стану в Git після відкату

```powershell
git revert HEAD --no-edit
git push

```

---

## 6. Очищення тестового навантаження після завершення демонстрації

```powershell
kubectl delete deployment load
```