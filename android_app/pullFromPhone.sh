adb ls /storage/emulated/0/GaitApp/Experiments/$1 |grep 'txt\|json'|awk '{print $4}' > files
rm -rf /home/sandeep/IMU/EXL_IMU/Experiments_BIMRA/Experiments_BIMRA_2/$1
mkdir /home/sandeep/IMU/EXL_IMU/Experiments_BIMRA/Experiments_BIMRA_2/$1
for i in $(cat files)
do
echo $i
adb pull /storage/emulated/0/GaitApp/Experiments/$1/$i /home/sandeep/IMU/EXL_IMU/Experiments_BIMRA/Experiments_BIMRA_2/$1
chown -R sandeep:sandeep /home/sandeep/IMU/EXL_IMU/Experiments_BIMRA/Experiments_BIMRA_2/$1
done

rm -f files
