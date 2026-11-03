# Helm-чарт для Medical User Service

Helm-чарт для розгортання мікросервісу **Medical Information System User Service** (`user-service`) та його залежності PostgreSQL у Kubernetes.

## Передумови

* **Кластер Kubernetes** версії 1.20+ (наприклад, Minikube, Kind)
* Встановлений **Helm** 3.x
* Налаштована утиліта **kubectl** для роботи з вашим кластером

## Репозиторій та залежності

Цей чарт включає **PostgreSQL 18.x** як зовнішню залежність чарта, яка керується через репозиторій Bitnami.

### Оновлення залежностей

Перед встановленням або рендерингом шаблонів оновіть залежності чарта:

```bash
helm dependency update chart/
```

## Конфігурація та середовища

Чарт містить окремі файли конфігурацій, адаптовані для розробки (`values-dev.yaml`) та продакшену (`values-prod.yaml`).

### Основні параметри (`values.yaml`)

| Параметр | Опис | Значення за замовчуванням |
| ----- | ----- | ----- |
| `replicaCount` | Кількість реплік подів | `1` |
| `image.repository` | Репозиторій Docker-образу | `medical-user-service` |
| `image.tag` | Тег Docker-образу | `1.0.0` |
| `image.pullPolicy` | Політика завантаження образу | `IfNotPresent` |
| `service.type` | Тип Kubernetes Service | `ClusterIP` |
| `service.port` | Порт сервісу | `8082` |
| `service.targetPort` | Порт контейнера застосунку | `8082` |
| `ingress.enabled` | Увімкнення маршрутизації Ingress | `false` |
| `ingress.host` | Хостнейм Ingress | `user-service.local` |
| `persistence.enabled` | Увімкнення Persistent Volume Claim | `true` |
| `persistence.size` | Обсяг виділеного сховища | `1Gi` |
| `postgresql.enabled` | Увімкнення розгортання Bitnami PostgreSQL | `true` |

## Інструкція з використання

### 1. Перевірка синтаксису та тестування шаблонів

Перевірте локальний синтаксис та згенеруйте маніфести локально:

```bash
# Перевірка синтаксису
helm lint chart/

# Тестове рендерення шаблонів для DEV-середовища
helm template test-dev chart/ -f chart/values-dev.yaml

# Тестове рендерення шаблонів для PROD-середовища
helm template test-prod chart/ -f chart/values-prod.yaml
```

### 2. Розгортання

#### Розгортання у DEV-середовище (`medical-dev`)

```bash
kubectl create namespace medical-dev

helm install medical-release chart/ \
  -f chart/values-dev.yaml \
  -n medical-dev
```

#### Розгортання у PROD-середовище (`medical-prod`)

```bash
kubectl create namespace medical-prod

helm install medical-release chart/ \
  -f chart/values-prod.yaml \
  -n medical-prod
```

## Управління життєвим циклом релізу

### Перегляд статусу релізу

```bash
helm list -n medical-dev
helm status medical-release -n medical-dev
```

### Оновлення релізу

Оновлення тегу образу застосунку або значень конфігурації динамічно:

```bash
# Оновлення тегу образу через прапорець --set
helm upgrade medical-release chart/ \
  -f chart/values-dev.yaml \
  --set image.tag="1.0.1" \
  -n medical-dev
```

### Історія ревізій та відкат (Rollback)

```bash
# Перегляд історії ревізій релізу
helm history medical-release -n medical-dev

# Відкат до першої ревізії
helm rollback medical-release 1 -n medical-dev
```

### Видалення релізу

```bash
helm uninstall medical-release -n medical-dev
```

## Доступ до застосунку

Після встановлення ви можете отримати доступ до застосунку за допомогою проксіювання портів:

```bash
# Проксіювання портів для ClusterIP
kubectl port-forward svc/medical-release-medical-user-service 8082:8082 -n medical-dev
```

Перевірка статусу здоров'я застосунку: `http://localhost:8082/actuator/health`