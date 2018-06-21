const javaMath = Java.type('java.lang.Math');

x = 0;
while (x < 1) {
	print("while" + x);
	x++;
}

for (i = 0; i < 1; i++) {
	print("for" + i);
}

y = 0;
do {
	print("do" + y);
	y++;
} while (y < 1)

function userDefinedFunction() {
	return javaMath.floor(javaMath.PI) + serverValue;
}

userDefinedFunction();