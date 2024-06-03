# react project build
cd ../prj2-react-20240521
yarn build

# index.html, main.js 복사(이동) : dist -> static
cd ../prj2-spring-20240521
rm -rf src/main/resources/static
mkdir src/main/resources/static
cp -r ../prj2-react-20240521/dist/* src/main/resources/static/.

# spring project build
./gradlew bootJar

# build/libs/prj.jar -> ec2 에 복사
scp -i src/main/resources/secret/key0527.pem build/libs/prj2-spring-20240521-0.0.1-SNAPSHOT.jar ubuntu@3.34.98.7:~/prj2/build/libs/.

# remote 에서
#ssh -i src/main/resources/secret/key0527.pem ubuntu@3.34.98.7 'chmod +x ~/prj2/remote.sh'
#ssh -i src/main/resources/secret/key0527.pem ubuntu@3.34.98.7 'bash ~/prj2/remote.sh'

# 이미 실행된 컨테이너 멈추고
ssh -i src/main/resources/secret/key0527.pem ubuntu@3.34.98.7 'docker stop my-prj2'
# 컨테이너 삭제
ssh -i src/main/resources/secret/key0527.pem ubuntu@3.34.98.7 'docker rm my-prj2'
# docker image 만들고
ssh -i src/main/resources/secret/key0527.pem ubuntu@3.34.98.7 'docker build -t my-prj2 ~/prj2/.'
# 컨테이너 실행
ssh -i src/main/resources/secret/key0527.pem ubuntu@3.34.98.7 'docker run -d -p 8080:8080 --restart always --name my-prj2 my-prj2'