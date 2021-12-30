import os

DEV_NULL = 'log.txt'
JAR_NAME1 = 'old.jar'
JAR_NAME2 = 'new.jar'

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
		os.system('java -jar ' + JAR_NAME1 + ' > ' + DEV_NULL)
		name1 = 'output_' + JAR_NAME1.split('.')[0] + '_' + str(i) + '.txt'
		if os.path.exists(name1):
			os.remove(name1)
		os.rename('output.txt', name1)
		print('---> ' + JAR_NAME1 + ' finished')
		########################################################################
		os.system('java -jar ' + JAR_NAME2 + ' > ' + DEV_NULL)
		name2 = 'output_' + JAR_NAME2.split('.')[0] + '_' + str(i) + '.txt'
		if os.path.exists(name2):
			os.remove(name2)
		os.rename('output.txt', name2)
		print('---> run ' + JAR_NAME2 + ' finished')
		########################################################################
		with open(name1, 'r') as file1:
			cont1 = file1.read()
		with open(name2, 'r') as file2:
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
