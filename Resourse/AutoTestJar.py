import os

DEV_NULL = 'log.txt'
JAR_NAME = 'Compiler.jar'

if __name__ == '__main__':
	diff = []
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
		name = 'output_' + JAR_NAME.split('.')[0] + '_' + str(i) + '.txt'
		if os.path.exists(name):
			os.remove(name)
		os.rename('output.txt', name)
		print('---> ' + JAR_NAME + ' finished')
		########################################################################
		with open(name, 'r') as file1:
			cont1 = file1.read()
		with open('output' + str(i) + '.txt', 'r') as file2:
			cont2 = file2.read()
		os.rename('testfile.txt', 'testfile' + str(i) + '.txt')
		if cont1 != cont2:
			diff.append('testfile' + str(i) + '.txt')
		print('---> compare finished, ' + str(cont1 == cont2))
		print('')
	os.remove('log.txt')
	if len(diff) == 0:
		print('^^^ all same ^^^')
	else:
		print('diff at ' + str(diff))
