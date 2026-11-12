# Покрокова інструкція для демонстрації та захисту лабораторної роботи (Canary Deployments з Argo Rollouts)

Цей документ містить повний сценарій демонстрації результатів лабораторної роботи: від перевірки архітектури GitOps та ресурсу `Rollout` до живого показу процесів просування релізу (**Promote**) та екстреного скасування з відкатом (**Abort & Git Revert**).

---

## 1. Підготовка робочого середовища перед захистом

Перед початком демонстрації відкрийте три вікна (або вкладки):
1. **Браузер:** веб-інтерфейс Argo CD (`http://localhost:8080`).
2. **Термінал 1 (Моніторинг):** для відображення стану Canary-розгортання в реальному часі.
3. **Термінал 2 (Керування):** для виконання команд `kubectl`, `kubectl argo rollouts` та `git`.

### Перевірка доступу до Argo CD UI (за потреби)
Якщо прокидання портів (port-forward) не активне, запустіть його у фоновому вікні:
```powershell
kubectl port-forward svc/argocd-server -n argocd 8085:443
```

---

## 2. Демонстрація поточної архітектури та GitOps-конфігурації

### Крок 2.1. Перевірка стану кластера та контролера
Покажіть викладачу, що у кластері розгорнуто контролер Argo Rollouts, а мікросервіс `user-service` працює як об'єкт `Rollout` (5 реплік) замість стандартного `Deployment`:

```powershell
# Перевірка CRD та контролера Argo Rollouts
kubectl get pods -n argo-rollouts

# Перевірка об'єкта Rollout та подів мікросервісу у namespace medical-system
kubectl get rollouts,pods -n medical-system
```
* **Очікуваний результат:** усі 5 подів `user-service-*` знаходяться у статусі `Running` (`READY 1/1`), а `Deployment` для `user-service` відсутній.

### Крок 2.2. Демонстрація Helm-шаблонів у репозиторії
Покажіть у IDE (або через термінал) ключові файли у гілці `devops/laba_5`:
1. `gitops/charts/user-service/templates/rollout.yaml` — шаблон `kind: Rollout` із налаштованими `startupProbe`, `readinessProbe`, `livenessProbe` та динамічною стратегією `{{- toYaml .Values.rollout.strategy | nindent 4 }}`.
2. `gitops/charts/user-service/templates/deployment.yaml` — обгорнутий в умову `{{- if not .Values.rollout.enabled }}`, що дозволяє перемикати стратегію без видалення шаблону.
3. `gitops/charts/user-service/values-minikube.yaml` — конфігурація Canary-кроків:
    * `setWeight: 20` $\rightarrow$ `pause: {}` (ручне підтвердження)
    * `setWeight: 60` $\rightarrow$ `pause: { duration: 30s }`
    * `setWeight: 80` $\rightarrow$ `pause: { duration: 30s }`

### Крок 2.3. Огляд у веб-інтерфейсі Argo CD
Відкрийте додаток **`user-service`** у браузері та зверніть увагу викладача на:
* Статус синхронізації: **`Synced`** та **`Healthy`**.
* Дерево ресурсів: корінь `user-service` $\rightarrow$ об'єкт **`Rollout`** $\rightarrow$ активний `ReplicaSet` $\rightarrow$ 5 `Pod`.

---

## 3. Жива демонстрація Canary-релізу (Promote)

### Крок 3.1. Запуск моніторингу (Термінал 1)
Запустіть інтерактивне спостереження за станом `Rollout`:
```powershell
kubectl argo rollouts get rollout user-service -n medical-system --watch
```

### Крок 3.2. Ініціювання нового релізу через Git (Термінал 2)
Змініть змінну середовища або версію у файлі `gitops/charts/user-service/values-minikube.yaml` (наприклад, оновіть `APP_VERSION: "1.1.1"` або тег образу, якщо зібрано новий) та відправте зміни в Git:
```powershell
git commit -am "Release user-service 1.1.1 for live Canary demo"
git push origin devops/laba_5
```
* **Що відбувається:** Argo CD синхронізує зміни. У **Терміналі 1** з'явиться нова ревізія (`canary`), створиться 1 новий под (20% від 5 реплік), а розгортання зупиниться зі статусом **`Paused (CanaryPauseStep)`** (у Argo CD UI статус зміниться на `Suspended`).

### Крок 3.3. Ручне підтвердження та просування релізу (Термінал 2)
Продемонструйте перехід релізу через решту етапів (60% $\rightarrow$ 80% $\rightarrow$ 100%):
```powershell
kubectl argo rollouts promote user-service -n medical-system
```
* **Що відбувається:** У **Терміналі 1** видно поступове масштабування нової версії (спочатку 3 поди, через 30 секунд — 4 поди, ще через 30 секунд — усі 5 подів) та автоматичне згортання старого `ReplicaSet`. Статус повертається у **`✔ Healthy`**.

---

## 4. Демонстрація аварійного скасування та GitOps-відкату (Abort & Revert)

Цей етап демонструє захист продакшен-середовища від невдалих релізів.

### Крок 4.1. Запуск "проблемного" релізу (Термінал 2)
У файлі `gitops/charts/user-service/values-minikube.yaml` вкажіть неіснуючий тег образу `"1.2.0-broken"`:
```yaml
image:
  repository: kirill1230507/medical-user-service
  tag: "1.2.0-broken"
  pullPolicy: IfNotPresent
```
Зафіксуйте та запуште зміну:
```powershell
git commit -am "Deploy broken version 1.2.0-broken"
git push origin devops/laba_5
```
* **Що відбувається:** У **Терміналі 1** з'являється Canary-под нової ревізії зі статусом `ImagePullBackOff` (або `ErrImagePull`), тоді як 4 стабільні поди попередньої версії продовжують безперебійно обслуговувати трафік.

### Крок 4.2. Екстрена зупинка релізу (Термінал 2)
Виконайте команду `abort`, щоб негайно припинити розгортання:
```powershell
kubectl argo rollouts abort user-service -n medical-system
```
* **Що відбувається:** Проблемний Canary-под миттєво видаляється, 100% трафіку повертається на стабільну ревізію, а `Rollout` переходить у стан **`Degraded (Aborted)`**.

### Крок 4.3. Синхронізація Git зі станом кластера (Git Revert)
Поясніть викладачу принцип **GitOps (Single Source of Truth)**: оскільки у Git досі вказано тег `1.2.0-broken`, необхідно відкотити коміт у репозиторії:
```powershell
git revert HEAD --no-edit
git push origin devops/laba_5
```
* **Очікуваний результат:** Argo CD підтягує повернуту стабільну конфігурацію, створює чисту ревізію та повертає додаток `user-service` у стан **`Synced`** та **`Healthy`**.

---

## 5. Підсумкова перевірка історії ревізій та контрольні запитання

Наприкінці демонстрації виведіть повну історію ревізій `Rollout` у терміналі:
```powershell
kubectl argo rollouts history user-service -n medical-system
```

### Короткі відповіді на типові запитання під час захисту:
1. **Чому замість `Deployment` використовується `Rollout`?**  
   Стандартний `Deployment` підтримує лише стратегії `RollingUpdate` та `Recreate`, які не дозволяють гнучко керувати відсотком трафіку або ставити розгортання на ручну паузу для перевірки метрик. Контролер Argo Rollouts додає CRD `Rollout` для реалізації Canary та Blue-Green деплойментів.
2. **Навіщо потрібна `startupProbe` у нашому шаблоні?**  
   Мікросервіс на Spring Boot при одночасному старті 5 реплік у середовищі Minikube завантажується близько 60–90 секунд. `startupProbe` блокує перевірки `livenessProbe` на час ініціалізації JVM (до 180 секунд), запобігаючи циклічним перезапускам (`CrashLoopBackOff`).
3. **Чому після `kubectl argo rollouts abort` обов'язково потрібно робити `git revert`?**  
   Argo CD постійно звіряє стан кластера з Git-репозиторієм. Команда `abort` змінює стан лише в кластері, тому без `git revert` додаток залишатиметься в статусі `Degraded` через розбіжність із бажаним станом, описаним у Git.