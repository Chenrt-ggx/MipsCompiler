import os

DEV_NULL = 'log.txt'
JAR_NAME = 'Compiler.jar'

if __name__ == '__main__':
	fileId = []
	for i in os.listdir():
		spl = i.split('.')
		if len(spl) == 2 and spl[1] == 'txt' and spl[0].startswith('testfile'):
			try:
				fileId.append(int(spl[0][8:]))
			except:
				pass
	fileId.sort()
	for i in fileId:
		print('--------- running ' + 'testfile' + str(i) + ' ---------')
		if os.path.exists('testfile.txt'):
			os.remove('testfile.txt')
		os.rename('testfile' + str(i) + '.txt', 'testfile.txt')
		print('---> init finished')
		########################################################################
		os.system('java -jar ' + JAR_NAME + ' > ' + DEV_NULL)
		name = 'output' + str(i) + '.txt'
		if os.path.exists(name):
			os.remove(name)
		os.rename('output.txt', name)
		os.rename('testfile.txt', 'testfile' + str(i) + '.txt')
		print('---> ' + JAR_NAME + ' finished')
		print('')
	os.remove('log.txt')
