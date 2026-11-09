# Лабораторна робота: Деплоймент мікросервісу `user-service` та PostgreSQL у Kubernetes (Minikube)

Цей репозиторій містить маніфести Kubernetes та інструкцію з розгортання мікросервісної архітектури системи медицини (`user-service` + `PostgreSQL`) у локальному кластері **Minikube**.

---

## 🛠 Технологічний стек

* **Java 25 / Spring Boot 3.x** (`user-service`)
* **PostgreSQL 18** (База даних)
* **Flyway** (Міграції бази даних)
* **Kubernetes (Minikube)** (Оркестрація контейнерів)
* **Docker** (Контейнеризація)

---

## 📁 Перелік параметрів у ConfigMap та Secret

Конфігурація застосунку розділена на неконфіденційні параметри (`ConfigMap`) та чутливі/захищені дані (`Secret`).

### 1. `medical-config` (ConfigMap)

Файл: `k8s/01-configmap.yaml`

| Змінна середовища | Значення за замовчуванням / Приклад | Опис |
| :--- | :--- | :--- |
| `DB_URL` | `jdbc:postgresql://postgres-service:5432/users_db` | URL-адреса підключення JDBC до БД PostgreSQL |
| `KEYCLOAK_ISSUER_URI` | `http://localhost:8081/realms/medical-realm` | URI видавця JWT-токенів (Keycloak Realm) |
| `APPOINTMENT_SERVICE_URL` | `http://appointment-service:8083` | Внутрішньокластерна URL-адреса мікросервісу прийомів |

### 2. `medical-secret` (Secret)

Файл: `k8s/02-secret.yaml` (Зберігаються у кодуванні `Base64`)

| Змінна середовища | Опис |
| :--- | :--- |
| `POSTGRES_USER` | Логін адміністратора бази даних PostgreSQL (наприклад, `postgres`) |
| `POSTGRES_PASSWORD` | Пароль користувача бази даних PostgreSQL |
| `DB_USERNAME` | Логін для підключення `user-service` до БД |
| `DB_PASSWORD` | Пароль для підключення `user-service` до БД |

---

## 📁 Структура маніфестів Kubernetes (`k8s/`)

1. `00-namespace.yaml` — Створення окремого простору імен `medical-system`.
2. `01-configmap.yaml` — Збереження загальних конфігурацій середовища.
3. `02-secret.yaml` — Збереження облікових даних бази даних.
4. `03-pvc.yaml` — PersistentVolumeClaim для постійного збереження даних PostgreSQL.
5. `04-postgres.yaml` — Deployment та ClusterIP Service для БД PostgreSQL.
6. `05-user-service.yaml` — Deployment (3 репліки) та ClusterIP Service для Spring Boot мікросервісу `user-service`.

---

## 🚀 Порядок розгортання (Deployment Order)

Для коректного запуску мікросервісної архітектури дотримуйтесь наступного чіткого порядку застосування маніфестів:

### Крок 1. Запуск кластера Minikube
Запустіть локальне середовище Kubernetes:
```powershell
minikube start
```

### Крок 2. Створення простору імен (Namespace)
Створіть ізольований namespace `medical-system`:
```powershell
kubectl apply -f k8s/00-namespace.yaml
```

### Крок 3. Створення конфігурацій (ConfigMap та Secret)
Завантажте параметри оточення та секрети до кластера перед запуском контейнерів:
```powershell
kubectl apply -f k8s/01-configmap.yaml
kubectl apply -f k8s/02-secret.yaml
```

### Крок 4. Створення тома для даних (PersistentVolumeClaim)
Створіть ресурс для збереження даних бази даних:
```powershell
kubectl apply -f k8s/03-pvc.yaml
```

### Крок 5. Розгортання бази даних PostgreSQL
Запустіть deployment та service бази даних:
```powershell
kubectl apply -f k8s/04-postgres.yaml
```

### Крок 6. Розгортання мікросервісу `user-service`
Запустіть основний backend-сервіс:
```powershell
kubectl apply -f k8s/05-user-service.yaml
```

> **Примітка:** Ви також можете розгорнути всі файли однойменною командою (Kubernetes застосує їх у порядку за залежностями):
> ```powershell
> kubectl apply -f k8s/
> ```

---

## 🧪 Тестування та Верифікація

### 1. Перевірка загального стану всіх ресурсів
```powershell
kubectl get all -n medical-system
```
*Очікуваний результат:* Поди `postgres` та 3 репліки `user-service` знаходяться у статусі `1/1 Running`.

### 2. Перевірка зчитування ConfigMap та Secret у контейнері (Завдання 5d)
```powershell
kubectl exec -it <pod-name> -n medical-system -- printenv DB_URL DB_USERNAME DB_PASSWORD
```

### 3. Перевірка доступу до REST API через Minikube Tunnel (Завдання 5c)
Запустіть тунель до сервісу:
```powershell
minikube service user-service -n medical-system
```
Виконайте тестовий HTTP-запит у сусідньому вікні PowerShell:
```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:<TUNNEL_PORT>/api/users/test"
```
*Очікуваний результат:* Статус `401 Unauthorized` (підтверджує коректну роботу сервера та Spring Security).

### 4. Масштабування та Self-healing (Завдання 6)
* **Масштабування до 3 реплік:**
  ```powershell
  kubectl scale deployment user-service --replicas=3 -n medical-system
  ```
* **Перевірка відновлення після збою (Self-healing):**
  ```powershell
  kubectl delete pod <pod-name> -n medical-system
  kubectl get pods -n medical-system
  ```

---

## 🧹 Очищення ресурсів

Зупинка кластера:
```powershell
minikube stop
```

Повне видалення створеного простору імен та всіх його ресурсів:
```powershell
kubectl delete namespace medical-system
```

```powershell
kubectl exec -it postgres-778c97bb8-9qpp4 -n medical-system -- createdb -U postgres new_database_name
```