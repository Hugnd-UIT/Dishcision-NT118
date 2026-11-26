import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('103.118.28.57', port=22, username='root', password='Dishcision2552@')

print('--> Pulling latest code on VPS...')
stdin, stdout, stderr = ssh.exec_command('cd /root/Dishcision && git fetch origin && git reset --hard origin/main')
print(stdout.read().decode())

print('--> Building and updating backend & frontend...')
stdin, stdout, stderr = ssh.exec_command('cd /root/Dishcision/docker && docker compose up -d --build backend frontend')
while True:
    line = stdout.readline()
    if not line:
        break
    print(line, end='')

print('\n--> Running seed to backfill audit logs...')
stdin, stdout, stderr = ssh.exec_command('docker exec -i dishcision-backend npx ts-node --compiler-options \'{"module":"commonjs"}\' prisma/seed.ts')
while True:
    line = stdout.readline()
    if not line:
        break
    print(line, end='')

ssh.close()
print('\n[OK] Deployed and seeded successfully!')
