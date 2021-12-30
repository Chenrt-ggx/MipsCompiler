import os

C_FILE = 'TestProgram'
JAR_NAME = 'Compiler.jar'
ADD_HEAD = '''
#include <stdio.h>

int getint() {
	int ret;
	scanf("%d", &ret);
	return ret;
}
'''

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
		os.system('java -jar ' + JAR_NAME + ' < input' + str(i) + '.txt > stdout' + str(i) + '.txt')
		name = 'output' + str(i) + '.txt'
		if os.path.exists(name):
			os.remove(name)
		os.rename('output.txt', name)
		print('---> run ' + JAR_NAME + ' finished')
		########################################################################
		with open('testfile.txt', 'r', errors='ignore') as file:
			code = file.read()
		with open(C_FILE + '.cpp', 'w') as file:
			file.write(ADD_HEAD + '\n' + code)
		os.system('g++ ' + C_FILE + '.cpp -o ' + C_FILE + ' -std=c++11')
		os.system('.\\' + C_FILE + ' < input' + str(i) + '.txt > answer' + str(i) + '.txt')
		print('---> run ' + C_FILE + '.cpp finished')
		########################################################################
		with open('answer' + str(i) + '.txt', 'r') as file1:
			cont1 = file1.read().strip()
			# cont1 = file1.read()
		with open('stdout' + str(i) + '.txt', 'r') as file2:
			cont2 = file2.read().strip()
			# cont2 = file2.read()
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
