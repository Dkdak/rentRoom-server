// 비동기 함수로 API 호출
async function fetchData(url) {
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error('Network response was not ok');
    }
    return await response.json();
}

async function fetchHtml(url) {
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error('Network response was not ok');
    }
    return await response.text();
}

// CLOB1 데이터 가져오기
async function loadClob1Data(id) {
    try {
        const response = await fetchData(`/api/clob1/${id}`);
        const headers = response.headers;
        const data = response.data;

        // 헤더 생성
        let columnIdHtml = '';
        headers.forEach(header => {
            columnIdHtml += `<th>${header.id}</th>`;
        });
        document.getElementById('columnIdHeader').innerHTML = columnIdHtml;

        let columnNameHtml = '';
        headers.forEach(header => {
            if (header.id.startsWith("resultFace")) {
                columnNameHtml += `<th class="bold-header">${header.name}</th>`;
            } else {
                columnNameHtml += `<th>${header.name}</th>`;
            }
        });
        document.getElementById('columnNameHeader').innerHTML = columnNameHtml;

        // 데이터 행 생성
        let rowsHtml = '';
        data.forEach((row, rowIndex) => {
            rowsHtml += '<tr>';
            headers.forEach(header => {
                rowsHtml += `<td><input type="text" value="${row[header.id]}" id="${header.id}_${rowIndex}"></td>`;
            });
            rowsHtml += '</tr>';
        });
        document.querySelector('#dataTable tbody').innerHTML = rowsHtml;

    } catch (error) {
        console.error('Error fetching CLOB1 data:', error);
    }
}

// CLOB2 데이터 가져오기
async function loadClob2Data(id) {
    try {
        const clob2Html = await fetchHtml(`/api/clob2/${id}`);
        document.getElementById('clob2Html').innerHTML = clob2Html;
    } catch (error) {
        console.error('Error fetching CLOB2 data:', error);
    }
}

// 새 행 추가
function addRow() {
    const headers = document.querySelectorAll('#columnIdHeader th');
    let newRowHtml = '<tr>';
    headers.forEach(header => {
        const headerId = header.innerText;
        newRowHtml += `<td><input type="text" value="" id="${headerId}_new"></td>`;
    });
    newRowHtml += '</tr>';
    document.querySelector('#dataTable tbody').insertAdjacentHTML('beforeend', newRowHtml);
}

// 데이터 저장
async function saveData(id) {
    try {
        let updatedData = [];
        document.querySelectorAll('#dataTable tbody tr').forEach((row, rowIndex) => {
            let rowData = {};
            row.querySelectorAll('td input').forEach(input => {
                const columnId = input.id.split('_')[0];
                rowData[columnId] = input.value;
            });
            updatedData.push(rowData);
        });

        const headers = [];
        document.querySelectorAll('#columnIdHeader th').forEach(header => {
            const headerId = header.innerText;
            const headerName = header.nextElementSibling ? header.nextElementSibling.innerText : '';
            headers.push({ id: headerId, name: headerName });
        });

        // JSON 데이터 생성
        const payload = { headers: headers, data: updatedData };

        // CLOB1에 JSON 저장하는 fetch 요청
        const saveClob1Response = await fetch(`/api/save/${id}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ data: payload })
        });

        if (!saveClob1Response.ok) {
            throw new Error('Failed to save CLOB1 JSON data');
        }

        // HTML 생성
        const htmlContent = `<html><head><title>Saved Table</title></head><body>${document.getElementById('dataTable').outerHTML}</body></html>`;

        // CLOB2에 HTML 저장
        const saveClob2Response = await fetch(`/api/save/clob2/${id}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ html: htmlContent })
        });

        if (!saveClob2Response.ok) {
            throw new Error('Failed to save CLOB2 HTML data');
        }

        alert('Data and HTML saved successfully');
    } catch (error) {
        console.error('Error saving data:', error);
    }
}

// 페이지 로드 시 데이터 가져오기
document.addEventListener('DOMContentLoaded', async () => {
    const id = 1; // 데이터 조회할 ID
    await loadClob1Data(id); // CLOB1 데이터 로드
    await loadClob2Data(id); // CLOB2 데이터 로드

    // 새 행 추가 버튼 이벤트 리스너
    document.getElementById('addRowBtn').addEventListener('click', addRow);

    // 데이터 저장 버튼 이벤트 리스너
    document.getElementById('saveBtn').addEventListener('click', () => saveData(id));
});
