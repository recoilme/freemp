## Общие принципы при написании кода

* Следуй принципу KISS [KISS (принцип)](https://ru.wikipedia.org/wiki/KISS_%28%D0%BF%D1%80%D0%B8%D0%BD%D1%86%D0%B8%D0%BF%29)
* Перед решением задачи - продумай все варианты, затем, выбери самый простой. Он будет верным.
* Не множь сущности сверх необходимого
* Не плоди интерфейсов над интерфейсами
* Не плоди классов там, где можно обойтись методами
* Не генери абстракций заранее, на всякий случай, жизнь богаче твоего воображения. Иногда бывают исключения, подтверждающие правило.
* Сложный код - плохой код
* Не бойся удалять код, нашел более простое решение - перепиши
* ООП - не панацея, а метод решить задачу проще. Используй его, тогда, и только тогда, когда с ним код будет лучше
* Держи классы большими,а методы короткими.
* Код из 100 классов читается сложнее, чем класс с 100 методами
* Если метод занимает больше 50-70 строк - декомпозируй его на логические куски.
* Используй модульную архитектуру. Все относящееся к модулю помещай в модуль. Делай модули независимыми друг от друга

## Структура приложения

* main - модули приложения
* test - тесты, покрывающие модули

## Правила именования

Во всем следует стремиться к единообразию.

### Правила именования классов

БазовыйклассПакет(Пояснение*)

Пример пакета offer:
* ActivityOffer
* PresenterOffer
* PresenterOfferImpl
* ViewOffer

Пояснение - необязательная часть

### Правила именования переменных

        public static List<Package> packages = new ArrayList<>();


не пиши sPackages. Не пиши mPackages - это мусор.

Не пиши listPackage. Не пиши arrayListPackages - завтра это изменится.

Не пиши list, data, rows - если там список из package - это ничего не скажет тому, кто это читает.

Переменная должна выражать суть, максимально коротким образом:

        Activity activity;
        app = (App) getApplication();
        nemoApi = app.nemoApi;
        preferences = app.getPreferences();

## Общее описание структуры пакетов

Каждый пакет - это не набор схожих сущностей (например Activity, Adapters), а отдельный модуль,
решающий определенную задачу. Все классы, решающие данную задачу должны быть собраны в одном
пакете, а не размазаны равномерно по проекту.

Пример:

- api
    - models/* - модели, для api
    - models/Category - модель для класса категорий
    - NemoApi - билдер, в котором собраны методы для генерации запросов к api
    - NemoBody - билдер, для генерации okHttp тела запроса
    - NemoClient - билдер, для генерации okHttp клиента
    - UtilsApi - утилиты, для работы с апи, и только для работы с апи

## Форматирование кода

Мы используем дефолтные настройки студии. При каждом коммите необходимо проверить, что проставлены
следующие галки
* reformat code
* rearrange code
* optimize imports
* perform code analysis
* check TODO

## Работа с репозиторием

Никто толком не знает как работает git. Это магия. Следствия:

* мы не ресетим и не ребэйзим git
* мы вообще не пользуемся никакими командами, кроме add, commit, push, pull, merge, checkout. Все остальные команды - тлен.
* ни у кого нет прав на запись в мастер
* у каждого программиста есть именная удаленная ветка, в которой он работает, например vadim, dasha
* иметь свои локальные ветки не запрещено, но и не поощряется. В любой момент любой человек должен
знать что он может переключиться на вашу ветку и посмотреть что у вас там происходит.
Если вы напочковали локальных брэнчей - будьте любезны сливать их в именную ветку.

Воркфлоу при работе с репозиторием - следующий:
* Придя с утра на работу - переключаемся на мастер - пулим изменения
* Переключаемся на именную ветку, мержимся с мастером, резолвим конфликты если необходимо
* Заходим в канбан доску - выбираем задачу, стартуем ее
* Пишем код, коммитя какие то логически завершенные группы изменений
* В коммите обязательно пишем пояснение что поменялось
* Завершив задачу - тестируем
* Прооверяем еще раз все ли за коммичено
* Переключаемся на мастер - пулим изменения из мастера
* Переключаемся на свою ветку и мержим ее с мастером
* Резолвим конфликты - если неообходимо
* Еще раз ТЕСТИРУЕМ
* Если все ок - создаем merge request
* Заходим в жиру, выбираем следующий таск и так по кругу
* Если приложение в мастере стало нерабочим - объявляется траур и мораторий на внесение дальнейших
изменений, пока не будет выясненно - кто накосячил. Избегайте этого.

## Реактивное программирование

В проекте активно используется концепция реактивного программирования. Чтобы лучше понять что происходит -
необходимо сначала ознакомиться с концепцией:

* http://reactivex.io/intro.html
* https://github.com/ReactiveX/RxJava/wiki

Слой бэкенда:

Все методы для работы с апи- есть observable. Они могут быть скомпанованы произвольным образом и упакованы в цепочку событий.

Слой фронтенда:

Слой интерфейса создает подписки на методы. Интерфейс ничего не знает о реализации, он просто подписывается на события, и ждет когда они произойдут.

## API

Для взаимодействия с сетью используется библиотека okHttp.

Для упрощения работы с okHttp создан пул классов - билдеров, которые являются по сути конструкторами запросов.
* NemoBody
* NemoClient
* NemoUrl

Все методы для взаимодействия с api собраны в классе NemoApi

В шапке класса перечислены все методы, например:

    //----------------------------------------------------------------------------------------------
    // Methods nemo.info.common
    private static final String GET_OFFER = "get_offer";
    private static final String GET_SIMPLE_BANNERS = "get_simple_banners";

Каждый метод, содержит шапку метода, с пояснением входящих параметров и типа возвращаемого результата

    /**
     * Возвращает список баннеров для зоны (планшеты используют promo_new)
     *
     * @return Observable that emits List<SimpleBanner>
     */
    public Observable<List<SimpleBanner>> getSimpleBanners() {
        Request request = new Request.Builder()
                .post(new NemoBody.Builder(GET_SIMPLE_BANNERS)
                        .param("zone", "promo_new")
                        .build())
                .url(new NemoUrl.Builder(PLUGIN_NEMO_INFO_COMMON)
                        .authKey(preferences.getString(UtilsApi.AUTHKEY, ""))
                        .build())
                .build();

        return new NemoClient.Builder()
                .build()
                .call(request)
                .map(s -> new Gson().fromJson(s, new TypeToken<List<SimpleBanner>>() {
                }.getType()));
    }

Структурно все методы состоят из билдеров запроса и билдера ответа.
Модели классов являются POJO объектами, без конструкторов. В качестве конструктора используется библиотека GSON.
Поля класса - зеркалируют модель объекта, приходящего в json. Поля именуются зеркально json модели,
так - как принято в javascript, например package_id. Данное упрощение сделано осознанно, для упрощения
понимания и минимизации кода, в соответствии с рекомендациями Square. Если значение в поле нельзя трактовать
однозначно - данное поле скрывается модификатором private - и для него пишется отдельный getter.
По умолчанию поля public. Пример класса:

    /**
     * Created by v.kulibaba on 21/09/15.
     * authkey":"%ключ сессии для дальнейшей работы с платформой%",
     * "balance":"%баланс пользователя%"
     * "analytics_id": "%id для статистики%"
     */
    public class Auth {
        public String authkey;
        public String balance;
        public String analytics_id;
    }

Пример класса с геттером

    public class Packages {
        public long id;
        public String description;

        public boolean have;
        public boolean no_renew;
        public int second_remains;

        public long valid_before;
        public Promo logo;
        private Map<String, JsonElement> plugin_options;

        public Map<String, List<Category>> getPluginOptions() {
            Map<String, List<Category>> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : plugin_options.entrySet()) {
                JsonElement val = entry.getValue();
                if (val.isJsonObject()) {
                    JsonElement category_list = val.getAsJsonObject().get("category_list");
                    if (category_list.isJsonArray()) {
                        map.put(entry.getKey(), new Gson().fromJson(category_list,
                                new TypeToken<List<Category>>() {
                                }.getType()));
                    }
                }
            }
            return map;
        }

    }

Все методы апи покрыты тестами. Пример теста:

    @Test
    public void getProgramsTest() {
        long[] programsIds = {28657438};
        String[] fields = {"record_status", "is_recordable"};
        Observable<List<Program>> observable = App.nemoApi.getPrograms(programsIds, fields);
        TestSubscriber<List<Program>> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        List<Program> result = testSubscriber.getOnNextEvents().get(0);
        assertNotNull(result);
        ShadowLog.w(TAG, "result.size() " + result.size());
    }

## UI

Слой UI построен на ООП. Точнее используется концепция View-Presenter. Для того, чтобы
понять как работает UI лучше сперва понять саму концепцию

https://corner.squareup.com/2014/10/advocating-against-android-fragments.html

Так выглядит типичный пакет для работы с интерфейсом модуля offer

Пример пакета offer:
* ActivityOffer
* PresenterOffer
* PresenterOfferImpl
* ViewOffer

Логика работы с данными - отделена от представления
Presenter - это интерфейс, пример

    /**
     * Created by d.efimova on 06.10.2015.
     */
    public interface PresenterOffer {

        public void loadText();

        public void registerUser();

        public void onPause();

        public void onResume();
    }

PresenterOfferImpl - его имплементация

Как правило, в имплементации - определен метод создания комплексной подписки на события, и отписка от событий

    @Override
    public void onPause() {
        compositeSubscription.unsubscribe();
        compositeSubscription = null;
    }

    @Override
    public void onResume() {
        if (compositeSubscription == null) {
            compositeSubscription = new CompositeSubscription();
        }
    }

Пример подписки на событие:

    @Override
    public void loadText() {
        if (compositeSubscription == null) {
            compositeSubscription = new CompositeSubscription();
        }
        compositeSubscription.add(nemoApi.getOffer()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Offer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        App.log(PresenterOfferImpl.class.getCanonicalName(), e);
                    }

                    @Override
                    public void onNext(Offer offer) {
                        if (mainView != null && offer != null) {
                            mainView.loadOfferText(offer.text);
                        }
                    }
                }));
    }

Подписка осуществляется в новом потоке. Результат возвращается в UI потоке. Полученные данные
передается в вью:

    @Override
    public void loadOfferText(String text) {
        WebView offerWebView = (WebView) findViewById(R.id.offer_web_view);
        offerWebView.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null);
    }

Таким образом слой вью может быть различным - для различных типов устройств (планшет, телефон, тв)

## Используемые библиотеки

    // библиотека совместимости
    compile 'com.android.support:appcompat-v7:23.0.1'
    // тв интерфейс
    compile 'com.android.support:leanback-v17:23.0.1'
    // работа с сетью
    compile 'com.squareup.okhttp:okhttp:2.5.0'
    // реактивная ява
    compile 'io.reactivex:rxjava:1.0.14'
    // набор классов специфических для андроид в связке с реактивным программированием
    compile 'io.reactivex:rxandroid:1.0.1'
    // конвертация json
    compile 'com.google.code.gson:gson:2.3.1'
    // работа с сетью через urlconnection интерфейс
    compile 'com.squareup.okhttp:okhttp-urlconnection:2.2.0'
    // видео плеер
    compile 'com.google.android.exoplayer:exoplayer:r1.4.0'
    // библиотека для тестирования без деплоя на устройство
    testCompile 'org.robolectric:robolectric:3.0'

## Узкие места

Часть данных хранится в глобальных массивах в классае Application. Это позволяет минимизировать
количество запросов к сети, для того чтобы отобразить ранее закешированные данные максимально быстро
с одной стороны. Но с другой - требует повышенной внимательности и аккуратности для поддержания
данных массивов в актуальном состоянии.

Используется модифицированная библиотека для загрузки изображений - с продвинутым алгоритмом кеширования
(две очереди). Сейчас это скомпилированный jar, обратно совместимый с Picasso. В дальнейшем планируется
написать собственное решение.

Не описано кеширование сетевых запросов, так как работы в этом направлении еще ведутся.

К сожалению код не всегда соответствует описанию выше. Но стремится к нему всей душой.

