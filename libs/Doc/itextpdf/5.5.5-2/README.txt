itextpdf 5.5.5:
�������� ������: https://github.com/itext/itextpdf.git � ��������� branch: 5.5.5
��������� � ������� ������ itext.

������� ������ itext � IDEA, ��������� JDK 7 � ������������ "clean compile install -Dhttps.protocols=TLSv1.2,TLSv1.1" � ����� itext. � ������������ ������ skip tests.

���� 0001-.patch ������� ���������, ��������, � ������� TortoiseGit: �� itextpdf/itext �������� ������ �������, ������� "TortoiseGit -> Apply Patch Serial...", ������� ���� � ��������� ���. � ������ ������ ��������� �������������.
��� ������������ � ����� itextpdf/itext:
git am --patch-format=mbox --3way --ignore-space-change --keep-cr <patch-file>