import os
import zipfile

IGNORE_SUBSTR = ['public']
IGNORE_ENDSTR = ['.md', '.mf', '.jar', '.pdf']

def checkPath(path):
	for i in IGNORE_ENDSTR:
		if path.lower().endswith(i):
			return False
	for i in IGNORE_SUBSTR:
		if i in path.lower():
			return False
	return True

def getFiles(target):
	files = []
	for i in os.listdir(target):
		if not i.startswith('.'):
			path = os.path.join(target, i)
			if os.path.isdir(path):
				files.extend(getFiles(path))
			elif os.path.isfile(path) and checkPath(path):
				files.append(path)
	return files

if __name__ == '__main__':
	os.chdir('..\\')
	pack = zipfile.ZipFile('..\\submit.zip', mode = 'w',\
		compression = zipfile.ZIP_DEFLATED, compresslevel = 9)
	for i in getFiles('.\\'):
		print('add ' + i)
		pack.write(i)
	print('pack finished')
	pack.close()
