# picketfence

## Sample App

### Clone the plugin and this sample app repo
```bash
git clone https://github.com/neatorobito/PicketFence
git clone https://github.com/neatorobito/Perimeter
```

### Build and install the plugin locally
```bash
cd Perimeter
npm install
npm run build

cd ../PicketFence
npm install ../Perimeter
```

### Build and run the sample app
```bash
npm install
npm run build

#If you hit a build error related to SSL, try using a legacy OpenSSL provider by setting the environment variable below. Note this is a temporary workaround and you may need to switch to a different version of Node.

# export NODE_OPTIONS=--openssl-legacy-provider

npx cap sync
npx cap open ios #or android
```