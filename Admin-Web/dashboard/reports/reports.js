
if(!userID) {
    window.location.replace("../../login/");
}

document.querySelector("tbody").innerHTML = "";
document.querySelector(".card-list").classList.add("loader");

let isDataTable = true;
fDatabase.ref('Garbage').on('value', (list) => {

    let html = "";
    let i = 0;
    list.forEach((item) => {

        const id = item.key;
        const data = item.val();

        if(userDistrict && !(""+data.district).includes(userDistrict)) {
            return;
        }

        i++;
        let html1 = `
            <tr>
                <td>
                    ${i}
                </td>
                <td>
                    ${data.name ?? '-'}
                </td>
                <td class="text-center">
                    ${data.phone ?? '-'}
                </td>
                <td class="text-center">
                    ${data.garbage ?? '-'}
                </td>
                <td class="text-center">
                    ${data.packages ?? '-'}
                </td>
                <td class="text-center">
                    ${data.amount ?? '-'} Rwf
                </td>
                <td class="text-center">
                    ${new Date().toString().substring(0, 24)}
                </td>
                <td class="text-center">
                    ${data.driverName ?? '-'}
                </td>
                <td class="text-center">
                    <a class="text-white ${data.isPicked ? 'btn-success' : 'btn-warning'}" style="padding: 5px 10px; border-radius: 10px;">
                        ${data.isPicked ? 'Picked' : 'Pending'}
                    </a>
                </td>
            </tr>
        `;

        html = html1 + html;
        
    });

    document.querySelector("tbody").innerHTML = html;

    if(isDataTable) {
        $('.table').DataTable({
        dom: 'Bfrtip',
        buttons: [
            {
                extend: 'excelHtml5',
                text: '<i class="fa fa-file-excel-o"></i> Excel',
                exportOptions: {
                    columns: ':not(.no-export)'
                }
            },
            {
                extend: 'pdfHtml5',
                text: '<i class="fa fa-file-pdf-o"></i> PDF',
                exportOptions: {
                    columns: ':not(.no-export)'
                }
            },
            {
                extend: 'print',
                text: '<i class="fa fa-print"></i> PRINT',
                exportOptions: {
                    columns: ':not(.no-export)'
                }
            },
        ]
        });
        isDataTable = false;
    }

    document.querySelector(".card-list").classList.remove("loader");

});