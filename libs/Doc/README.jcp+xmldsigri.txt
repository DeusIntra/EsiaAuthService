Для запуска тестов следует:

1) инсталлировать CryptoPro JCP.
2) сформировать ключ и сертификат клиента на алгоритме ГОСТ Р 34.10.
3) запустить IDE Eclipse, создать пустой проект и поместить в папку src папку ru со всеми содержащимися в ней папками и файлами. В свойствах проекта следует добавить ссылку на CryptoProXMLDSigRI-1.51.jar и jar-файлам:
axis-1.4.jar
commons-logging-1.1.jar
serializer-2.7.1.jar
xalan-2.7.1.jar
xmlsec-1.5.0.jar

ВНИМАНИЕ! Для работы CryptoProXMLDSigRI-1.51 с wss4j 1.6.3 нужно использовать xmlsec-1.5.0.jar.

4) В папке с проектом должен быть создан каталог WebContent/resources, в него помещен файл crypto.properties с необходимыми настройками. Например:

org.apache.ws.security.crypto.provider=ru.wss4j1_6_3.ws.security.components.crypto.MerlinEx
org.apache.ws.security.crypto.merlin.keystore.type=HDImageStore
org.apache.ws.security.crypto.merlin.keystore.password=my_password
org.apache.ws.security.crypto.merlin.keystore.alias=my_key_store
cert.file=path_to_cert

,где my_password - пароль для доступа к контейнеру, 
my_key_store - название контейнера (alias), 
path_to_cert - путь к сертификату, соответствующему my_key_store.

Примечание #1:
CryptoProXMLDSigRI-1.51 поддерживает алгоритмы -
1) хэширования http://www.w3.org/2001/04/xmldsig-more#gostr3411
2) ЭЦП http://www.w3.org/2001/04/xmldsig-more#gostr34102001-gostr3411

Примечание #2:
Использовать сервис-провайдер CryptoProXMLDSigRI-1.51 нужно так, как это показано в тестах jcp+xmldsigri, то есть так:
	...
	// Формируем сервис-провайдер Крипто Про
	Provider pxml = ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI();
	// Указываем на него, создав factory для подписи/проверки
	XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM", pxml);
	...

Примечание #3:
в файле ru.CryptoPro.JCPxml.dsig.internal.utility.SpecUtility имеется ряд переменных, которые загружаются из {user.dir}/WebContent/resources/crypto.properties.

Внимание! При возникновении исключений, вызванных классом Transform, следует перед использованием провайдера в wss4j 1.6.3 поставить строчку инициализации (см. http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6982772):
com.sun.org.apache.xml.internal.security.Init.init();

Внимание! Если возникло исключение типа algorithm already registered, class not found (serializer, uri, looger) то необходимо поместить в папку с установленным CryptoPro JCP (jre/lib/ext) файлы:
commons-logging-1.1.jar
serializer-2.7.1.jar
xalan-2.7.1.jar
xmlsec-1.5.0.jar

##################################################################################################

Описание пакетов:

1. ru.CryptoPro.JCPxml.dsig.internal.xmldsigri.tests - содержатся 2 пары примеров использования сервис-провайдера CryptoProXMLDisgRI: 
два на подпись и проверку ЭЦП с добавлением в XML открытого ключа, и два на подпись и проверку ЭЦП с добавлением в XML сертификата. Вывод подписанных XML осуществляется в файл (следует изменить путь к файлу на требуемый), чтение подписанного XML для проверки также осуществляется из файла (следует изменить путь к файлу на требуемый).

##################################################################################################

Описание тестов:

1) GenDetached.java - подпись генерируемого XML документа сгенерированным ключом (генерируется пара ключей).
2) GenDetachedByCert.java - подпись генерируемого XML документа клиентским ключом, сформированным раннее в панели ControlPane JCP.
3) Validate.java - проверка ЭЦП подписанного XML документа открытым ключом, имеющемся внутри.
4) ValidateByCert.java - проверка ЭЦП подписанного документа открытым ключом сертификата, находящимся внутри.

