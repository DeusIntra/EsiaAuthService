itextpdf 5.5.5:
Забирать отсюда: https://github.com/itext/itextpdf.git с указанием branch: 5.5.5
Открывать и патчить проект itext.

Открыть проект itext в IDEA, настроить JDK 7 и конфигурацию "clean compile install -Dhttps.protocols=TLSv1.2,TLSv1.1" в папке itext. В конфигурации задать skip tests.

Патч 0001-.patch следует применять, например, с помощью TortoiseGit: на itextpdf/itext кликнуть правой кнопкой, выбрать "TortoiseGit -> Apply Patch Serial...", выбрать патч и применить его. В случае успеха изменение зафиксируется.
Или использовать в папке itextpdf/itext:
git am --patch-format=mbox --3way --ignore-space-change --keep-cr <patch-file>