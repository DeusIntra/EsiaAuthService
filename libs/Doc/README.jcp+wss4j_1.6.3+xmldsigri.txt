Для запуска тестов следует:

1) инсталлировать CryptoPro JCP.
2) сформировать ключ и сертификат клиента на алгоритме ГОСТ Р 34.10.
3) запустить IDE Eclipse, создать пустой проект и поместить в папку src папку wss4j со всеми содержащимися в ней папками и файлами. В свойствах проекта следует добавить путь к XMLDSigRI.jar и jar-файлам wss4j 1.6.3:
axis-1.4.jar
axis-jaxrpc-1.4.jar
bcmail-jdk16-146.jar
bcprov-jdk15-1.46.jar
commons-discovery-0.2.jar
commons-logging-1.1.1.jar
joda-time-1.6.2.jar
junit-4.8.2.jar
log4j-1.2.16.jar
opensaml-2.5.1-1.jar
openws-1.4.2-1.jar
serializer-2.7.1.jar
slf4j-api-1.6.1.jar
slf4j-log4j12-1.6.1.jar
wss4j-1.6.3.jar
xalan-2.7.1.jar
xmlsec-1.5.0.jar
xmltooling-1.3.2-1.jar

ВНИМАНИЕ! Для работы XMLDSigRI.jar с wss4j 1.6.3 нужно использовать xmlsec-1.5.0.jar.

4) В папке с проектом должен быть создан каталог /data/WebContent, в него помещен файл crypto.properties с необходимыми настройками. Например:

org.apache.ws.security.crypto.provider=wss4j.wss4j1_6_3.ws.security.components.crypto.MerlinEx
org.apache.ws.security.crypto.merlin.keystore.type=HDImageStore
org.apache.ws.security.crypto.merlin.keystore.password=my_password
org.apache.ws.security.crypto.merlin.keystore.alias=my_key_store
cert.file=path_to_cert
ca.file=path_to_ca
crl.file=path_to_crl

,где my_password - пароль для доступа к контейнеру, 
my_key_store - название контейнера (alias), 
path_to_cert - путь к сертификату, соответствующему my_key_store,
path_to_ca - путь к корневому сертификату для path_to_cert,
path_to_crl - путь к CRL файлу.

Примечание #1:
также может присутствовать параметр org.apache.ws.security.crypto.merlin.file.

Примечание #2:
в файле wss4j.utility.SpecUtility имеется ряд переменных, которые загружаются из {user.dir}/data/WebContent/crypto.properties.

Примечание #3:
XMLDSigRI поддерживает алгоритмы -
1) хэширования http://www.w3.org/2001/04/xmldsig-more#gostr3411 или urn:ietf:params:xml:ns:cpxmlsec:algorithms:gostr3411
2) ЭЦП http://www.w3.org/2001/04/xmldsig-more#gostr34102001-gostr3411 или urn:ietf:params:xml:ns:cpxmlsec:algorithms:gostr34102001-gostr3411


Примечание #4:
чтобы выполнялась подпись и проверка ЭЦП ГОСТ 34.10, необходимо задействовать в своем проекте сервис-провайдер XMLDSigRI следующими способами:
1) Подменой классов:
	...
 	Provider pNew = new ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI();
	Security.addProvider(pNew);
	...
	Security.getProvider("XMLDSig").put("XMLSignatureFactory.DOM", 
		        "ru.CryptoPro.JCPxml.dsig.internal.dom.DOMXMLSignatureFactory");
	Security.getProvider("XMLDSig").put("KeyInfoFactory.DOM", 
		        "ru.CryptoPro.JCPxml.dsig.internal.dom.DOMKeyInfoFactory");
	...	
	Этот вариант подходит, когда в силу каких-то причин вы не можете использовать вариант 2, например, для теста в 	пакете wss4j.ws4j1_6_3.bad.TestDubl.
2) явно использовать сервис-провайдер, как это показано в тестах jcp+xmldsigri, то есть так:
	...
	// Формируем сервис-провайдер Крипто Про
	Provider pxml = new ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI();
	// Указываем на него, создав factory для подписи/проверки
	XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM", pxml);
	...
	Пример имеется в SOAPXMLSignatureManager_1_6_3, который описан ниже.

Внимание! При возникновении исключений, вызванных классом Transform, следует перед использованием провайдера в wss4j 1.6.3 поставить строчку (см. http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6982772):
com.sun.org.apache.xml.internal.security.Init.init();

Внимание! Если возникло исключение типа algorithm already registered, class not found (serializer, uri, looger) то необходимо поместить в папку с установленным CryptoPro JCP (jre/lib/ext) файлы:
commons-logging-1.1.1.jar
serializer-2.7.1.jar
xalan-2.7.1.jar
xmlsec-1.5.0.jar

##################################################################################################

Описание пакетов:

wss4j.manager.* - классы с описанием потоков, тестирующих функции формирования/проверки ЭЦП XML SOAP.
wss4j.wss4j1_6_3.ws.security.components.crypto - содержит расширенный класс MerlinEx.
wss4j.wss4j1_6_3.tests - пакет с тестами производительности JCP.
wss4j.wss4j1_6_3.tests.forum - пакет с примерами WSS4J + JCP, обсуждаемыми на форуме.
wss4j.wss4j1_6_3.manager.SOAPXMLSignatureManager_1_6_3 - класс примера формирования XML SOAP документа, подписания и проверки ЭЦП.

##################################################################################################

Описание тестов производительности:

1. Добавлен потомок MerlinEx класса Merlin для кэширования закрытого ключа и ускорения подписания. Соответственно, изменен параметр org.apache.ws.security.crypto.provider в файле crypto.properties. Его новое значение:
org.apache.ws.security.crypto.provider=wss4j.wss4j1_6_3.ws.security.components.crypto.MerlinEx

2. Добавлены несколько видов тестов для оценки скорости подписывания и проверки ЭЦП (wss4j.wss4j1_6_3.tests). 
Увеличение числа потоков может привести к росту производительности.

 А. EfficiencyTestSingle оценивает скорость формирования и проверки ЭЦП в SOAP XML, выполняемых одна за другой в одном блоке в последовательном цикле в главном потоке приложения; дается более точная оценка времени выполнения каждой операции (op/s) и средняя скорость выполнения (op/s).
Пример для цикла из 1000 итераций 
1) Средняя скорость пары операций: 60 оп/с (Windows 7 32-b., 1-яд. проц., 2 Гб ОЗУ)
2) Средняя скорость операции подписывания: 50 оп/с (Windows 7 32-b., 1-яд. проц., 2 Гб ОЗУ)
3) Средняя скорость операции проверки ЭЦП: 70 оп/с (Windows 7 32-b., 1-яд. проц., 2 Гб ОЗУ)

 Б. EfficiencyTestCombined оценивает скорость формирования и проверки ЭЦП в SOAP XML, выполняемых одна за другой в одном блоке в разных потоках; кол-во потоков можно менять; дается средняя скорость выполнения (op/s) операций, причем она выше скорости в EfficiencyTestSingle. 
Пример для 5 запущенных потоков 
1) Средняя скорость пары операций: 100 оп/с (Windows 7 32-b., 1-яд. проц., 2 Гб ОЗУ)
2) Средняя скорость операции подписывания: 75 оп/с (Windows 7 32-b., 1-яд. проц., 2 Гб ОЗУ)
3) Средняя скорость операции проверки ЭЦП: 118 оп/с (Windows 7 32-b., 1-яд. проц., 2 Гб ОЗУ)

 В. EfficiencyTestMulti оценивает скорость формирования и проверки ЭЦП в SOAP XML, выполняемых независимо друг от друга в разных потоках, но синхронизированных по очереди сообщений; кол-во потоков можно менять; дается средняя скорость выполнения (op/s) операций, которая приближается к аналогичной скорости в EfficiencyTestSingle из-за синхронизации потоков по одной общей для всех потоков очереди.
Пример для 3 потоков-подписчиков и 3 потоков-проверяльщиков (10 потоков)
1) средняя скорость пары операций: 57 оп/с (Windows 7 32-b., 1-яд. проц., 2 Гб ОЗУ)
2) средняя скорость операции подписывания: 46 оп/с (Windows 7 32-b., 1-яд. проц., 2 Гб ОЗУ)
3) средняя скорость операции проверки ЭЦП: 67 оп/с (Windows 7 32-b., 1-яд. проц., 2 Гб ОЗУ)

 Г. EfficiencyTestMultiQueue оценивает скорость формирования и проверки ЭЦП в SOAP XML, выполняемых независимо друг от друга в разных потоках, но синхронизированных попарно по своей очереди сообщений (поток-подписчик и поток-проверяльщик имеют одну общую очередь, таких пар может быть несколько); кол-во пар потоков можно менять; дается средняя скорость выполнения (op/s) операций, которая приближается к аналогичной скорости в EfficiencyTestCombined из-за синхронизации пар потоков по их общей очереди и может выше.
Пример для 3 пар потоков-подписчиков и потоков-проверяльщиков (6 потоков)
1) средняя скорость пары операций: 101 оп/с (Windows 7 32-b., 1-яд. проц., 2 Гб ОЗУ)
2) средняя скорость операции подписывания: 77 оп/с (Windows 7 32-b., 1-яд. проц., 2 Гб ОЗУ)
3) средняя скорость операции проверки ЭЦП: 122 оп/с (Windows 7 32-b., 1-яд. проц., 2 Гб ОЗУ)

Тесты показали лучшую производительность для wss4j 1.6.3 по сравнению с wss4j 1.5.11 (см. Примечание #1 ниже).

3. Добавление класса SOAPXMLSignatureManager_1_6_3 для формирования и проверки ЭЦП в SOAP XML документах и для использования его в простых примерах, выполняющихся в главном потоке (например, в тесте WSS4J_SignVerifySOAP).

4. Пример для разового подписывания/проверки ЭЦП SOAP XML документа в пакет wss4j.wss4j1_6_3.tests: WSS4J_SignVerifySOAP. 

Примечание #1:
Класс SOAPXMLSignatureManager_1_6_3 может показать не очень высокие результаты подписывания, т.к. для интеграции в тесты производительности добавлены некоторые лишние преобразования, которые в своем рабочем проекте можно удалить.

##################################################################################################

Описание проверки цепочки доверия:

1. Примеры для проверки цепочки сертификатов в пакете wss4j.wss4j1_6_3.tests: ValidateCertificateChain.
Представлены 2 метода:
1) функции проверки цепочки сертификатов с CRL (runTestRSA - для ключей RSA, runTestGOST - для ключей ГОСТ Р 34.10). Сертификаты загружаются из ключевого контейнера или файлов.
2) пример применения метода validateCertPath из интерфейса Crypto (CRL отключен) (runTestRSA_IfCAIsInCacertsAndIfUseMerlinByProperties и
runTestGOST_IfCAIsInCacertsAndIfUseMerlinByProperties). Сертификаты загружаются из ключевого контейнера и хранилища корневых сертификатов JRE_HOME/lib/security/cacerts.
