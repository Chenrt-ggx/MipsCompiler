import os

def getFiles(target):
	files = []
	for i in os.listdir(target):
		if not i.startswith('.'):
			path = os.path.join(target, i)
			if os.path.isdir(path):
				files.extend(getFiles(path))
			elif os.path.isfile(path) and path.endswith('.java'):
				files.append(path)
	return files

if __name__ == '__main__':
	os.chdir('..\\')
	for i in getFiles('.\\'):
		print('checking ' + i)
		with open(i, 'r') as file:
			text = file.read().split('public')[0].split('\n\n')
			text = filter(lambda x: x.startswith('import'), text)
			for i in text:
				lens = []
				for j in i.split('\n'):
					lens.append(len(j))
				if not all([lens[j] <= lens[j + 1] for j in range(len(lens) - 1)]):
					print('bad import style at file ' + file.name + ':')
					print(i)
	print('check finished')
