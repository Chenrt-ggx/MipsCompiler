import os

HAS_STRIP = True
C_FILE = 'TestProgram'
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
		os.system('java -jar ' + JAR_NAME + ' > ' + C_FILE + '.cpp')
		if os.path.exists('output.txt'):
			os.remove('output.txt')
		print('---> ' + JAR_NAME + ' finished')
		########################################################################
		os.system('g++ ' + C_FILE + '.cpp -o ' + C_FILE + ' -std=c++11')
		os.system('.\\' + C_FILE + ' < input' + str(i) + '.txt > answer' + str(i) + '.txt')
		print('---> run ' + C_FILE + '.cpp finished')
		########################################################################
		with open('answer' + str(i) + '.txt', 'r') as file1:
			if HAS_STRIP:
				cont1 = file1.read().strip()
			else:
				cont1 = file1.read()
		with open('output' + str(i) + '.txt', 'r') as file2:
			if HAS_STRIP:
				cont2 = file2.read().strip()
			else:
				cont2 = file2.read()
		os.rename('testfile.txt', 'testfile' + str(i) + '.txt')
		if cont1 != cont2:
			diff.append('testfile' + str(i) + '.txt')
		print('---> compare finished, ' + str(cont1 == cont2))
		print('')
	os.remove('TestProgram.cpp')
	os.remove('TestProgram.exe')
	if len(diff) == 0:
		print('^^^ all same ^^^')
	else:
		print('diff at ' + str(diff))
