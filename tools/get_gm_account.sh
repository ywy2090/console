#!/bin/bash

set -e
pkcs12_file=""
keccak256sum=""
output_path="accounts_gm"
sm3_bin="/tmp/sm3"
sm3_tar="/tmp/sm3.tgz"
TASSL_CMD="${HOME}"/.tassl

help() {
    echo $1
    cat << EOF
Usage: $0 
    default       generate account and store private key in PEM format file
    -p            generate account and store private key in PKCS12 format file
    -k [FILE]     calculate address of PEM format [FILE]
    -P [FILE]     calculate address of PKCS12 format [FILE]
    -h Help
EOF

exit 0
}

prepare_keccak256()
{
    if [[ ! -f ${sm3_bin} ]];then
        if [ "$(uname)" == "Darwin" ];then
            // TODO: add support for macOS
            LOG_INFO "macOS is not supported yet."
            exit 0
            keccak256sum=""
            echo ${keccak256sum} | base64 -D - > ${sm3_tar}
        else
            echo ${keccak256sum} | base64 -d - > ${sm3_tar}
        fi
        tar -zxf ${sm3_tar} -C /tmp && rm ${sm3_tar}
        chmod u+x ${sm3_bin}
    fi
    mkdir -p ${output_path}
}

# TASSL env
check_and_install_tassl()
{
    if [ ! -f "${HOME}/.tassl" ];then
        curl -LO https://github.com/FISCO-BCOS/LargeFiles/raw/master/tools/tassl.tar.gz
        LOG_INFO "Downloading tassl binary ..."
        tar zxvf tassl.tar.gz
        chmod u+x tassl
        mv tassl ${HOME}/.tassl
    fi
}

LOG_INFO()
{
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

check_env() {
    check_and_install_tassl
}

calculate_address_pem()
{
    local pem_file=$1
    local no_print="$2"
    local suffix=${pem_file##*.}
    if [[ "${suffix}" != "pem" ]];then
        echo "The suffix of ${pem_file} is not pem. Please check it."
        exit 1
    fi
    prepare_keccak256
    privKey=$(${TASSL_CMD} ec -in ${pem_file} -text -noout 2>/dev/null| sed -n '3,5p' | tr -d ": \n" | awk '{print $0}')
    pubKey=$(${TASSL_CMD} ec -in ${pem_file} -text -noout 2>/dev/null| sed -n '7,11p' | tr -d ": \n" | awk '{print substr($0,3);}')
    echo "public key = ${pubKey}"
    accountAddress=$(${sm3_bin}  ${pubKey})
    [ ! -z "${no_print}" ] || LOG_INFO "Account Address   : 0x${accountAddress}"
}

calculate_address_pkcs12()
{
    local p12_file=$1
    local pem_file="/tmp/.tmp.pem"
    local suffix=${p12_file##*.}
    if [[ "${suffix}" != "p12" && "${suffix}" != "pfx" ]];then
        echo "The suffix of ${p12_file} is neither p12 nor pfx. Please check it."
        exit 1
    fi
    ${TASSL_CMD} pkcs12 -in ${p12_file} -out ${pem_file} -nodes
    calculate_address_pem ${pem_file}
    rm ${pem_file}
}

generate_gmsm2_param()
{
    local output=$1
    cat << EOF > ${output} 
-----BEGIN EC PARAMETERS-----
BggqgRzPVQGCLQ==
-----END EC PARAMETERS-----
EOF
}

main()
{
    while getopts "k:pP:h" option;do
        case $option in
        k) calculate_address_pem "$OPTARG"
        exit 0;;
        P) calculate_address_pkcs12 "$OPTARG"
        exit 0;;
        p) #pkcs12_file="$OPTARG"
        pkcs12_file="true"
        ;;
        h) help;;
        esac
    done
    check_env
    prepare_keccak256
    if [ ! -f /tmp/gmsm2.param ];then
        generate_gmsm2_param /tmp/gmsm2.param
    fi
    ${TASSL_CMD} genpkey -paramfile /tmp/gmsm2.param -out ${output_path}/ecprivkey.pem 2>/dev/null
    calculate_address_pem ${output_path}/ecprivkey.pem "true"
    if [ -z "$pkcs12_file" ];then
        mv ${output_path}/ecprivkey.pem ${output_path}/0x${accountAddress}.pem
        LOG_INFO "Account Address   : 0x${accountAddress}"
        LOG_INFO "Private Key (pem) : ${output_path}/0x${accountAddress}.pem"
        # echo "0x${privKey}" > ${output_path}/${accountAddress}.private.hex
        ${TASSL_CMD} ec -in ${output_path}/0x${accountAddress}.pem -pubout -out ${output_path}/0x${accountAddress}.public.pem 2>/dev/null
        LOG_INFO "Public  Key (pem) : ${output_path}/0x${accountAddress}.public.pem"
    else
        ${TASSL_CMD} pkcs12 -export -name key -nocerts -inkey "${output_path}/ecprivkey.pem" -out "${output_path}/0x${accountAddress}.p12" || $(rm ${output_path}/0x${accountAddress}.p12 && rm ${output_path}/ecprivkey.pem && exit 1)
        ${TASSL_CMD} ec -in ${output_path}/ecprivkey.pem -pubout -out ${output_path}/0x${accountAddress}.public.p12 2>/dev/null
		rm ${output_path}/ecprivkey.pem
        LOG_INFO "Account Address   : 0x${accountAddress}"
        LOG_INFO "Private Key (p12) : ${output_path}/0x${accountAddress}.p12"
		LOG_INFO "Public  Key (p12) : ${output_path}/0x${accountAddress}.public.p12"
    fi
    # LOG_INFO "Private Key (hex) : 0x${privKey}"
    # echo "0x${pubKey}" > ${output_path}/${accountAddress}.public.hex
    # LOG_INFO "Public  File(hex) : ${output_path}/${accountAddress}.public.hex"
}

main $@
