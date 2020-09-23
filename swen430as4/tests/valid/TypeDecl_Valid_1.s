
	.text
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $32, %rsp
	movq $24, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $2, %rbx
	movq %rbx, 0(%rax)
	movq %rax, -8(%rbp)
	movq $1, %rbx
	movq %rbx, 8(%rax)
	movq $2, %rbx
	movq %rbx, 16(%rax)
	movq -8(%rbp), %rax
	movq 8(%rax), %rax
	movq %rax, -16(%rbp)
	movq -16(%rbp), %rax
	movq $1, %rbx
	cmpq %rax, %rbx
	jnz label712
	movq $1, %rax
	jmp label713
label712:
	movq $0, %rax
label713:
	movq %rax, %rdi
	call assertion
	movq -8(%rbp), %rax
	movq 16(%rax), %rax
	movq $2, %rbx
	cmpq %rax, %rbx
	jnz label714
	movq $1, %rax
	jmp label715
label714:
	movq $0, %rax
label715:
	movq %rax, %rdi
	call assertion
	movq $3, %rbx
	cmpq %rax, %rbx
	jnz label716
	movq $1, %rax
	jmp label717
label716:
	movq $0, %rax
label717:
	movq %rax, %rdi
	call assertion
	movq -24(%rbp), %rax
	movq $0, %rbx
	shlq %rbx
	shlq %rbx
	shlq %rbx
	addq $8, %rbx
	addq %rbx, %rax
	movq 0(%rax), %rax
	movq $1, %rbx
	cmpq %rax, %rbx
	jnz label718
	movq $1, %rax
	jmp label719
label718:
	movq $0, %rax
label719:
	movq %rax, %rdi
	call assertion
	movq -24(%rbp), %rax
	movq $1, %rbx
	shlq %rbx
	shlq %rbx
	shlq %rbx
	addq $8, %rbx
	addq %rbx, %rax
	movq 0(%rax), %rax
	movq $2, %rbx
	cmpq %rax, %rbx
	jnz label720
	movq $1, %rax
	jmp label721
label720:
	movq $0, %rax
label721:
	movq %rax, %rdi
	call assertion
	movq -24(%rbp), %rax
	movq $2, %rbx
	shlq %rbx
	shlq %rbx
	shlq %rbx
	addq $8, %rbx
	addq %rbx, %rax
	movq 0(%rax), %rax
	movq $3, %rbx
	cmpq %rax, %rbx
	jnz label722
	movq $1, %rax
	jmp label723
label722:
	movq $0, %rax
label723:
	movq %rax, %rdi
	call assertion
label711:
	movq %rbp, %rsp
	popq %rbp
	ret
	.globl main
main:
	pushq %rbp
	call wl_main
	popq %rbp
	movq $0, %rax
	ret

	.data
