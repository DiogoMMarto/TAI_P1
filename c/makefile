COMPILER=cc
FILE=fcm.c

fcm:
	${COMPILER} ${FILE} -Wall -Wextra -o3 -lm -march=native -mtune=native -flto -ffast-math -fomit-frame-pointer -funroll-loops -o fcm

fcm_debug:
	${COMPILER} ${FILE} -Wall -Wextra -ggdb -lm -o fcm_debug

clean:
	rm -rf fcm fcm_debug

all: clean fcm fcm_debug